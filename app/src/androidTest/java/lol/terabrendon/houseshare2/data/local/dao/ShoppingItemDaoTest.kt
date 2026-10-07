package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.GroupMember
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.local.database.HouseShareDatabaseV2
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository.Sorting
import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Shopping items are owned by group members, and a member does not need an account.
 */
@RunWith(AndroidJUnit4::class)
class ShoppingItemDaoTest {
    private lateinit var db: HouseShareDatabaseV2
    private lateinit var dao: ShoppingItemDao

    private val groupId = 1L

    // Member ids deliberately differ from the user ids.
    private val zoeMember = 10L     // account "zoe"
    private val adamMember = 11L    // no account

    @Before
    fun setUp() = runBlocking<Unit> {
        db = Room
            .inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                HouseShareDatabaseV2::class.java,
            )
            .build()
        dao = db.shoppingItemDao()

        db.userDao().upsert(User(100, "zoe", null, "Zoe", null, null))
        db.groupDao().upsert(Group(groupId, "Flat", null, null))
        db.groupMemberDao().upsertAll(
            listOf(
                GroupMember(zoeMember, "Zoe", null, groupId, null, 100),
                GroupMember(adamMember, "Adam", null, groupId, null, null),
            )
        )

        val now = LocalDateTime.of(2026, 10, 1, 12, 0)
        dao.insert(item(1, "Milk", zoeMember, now))
        dao.insert(item(2, "Bread", adamMember, now.plusHours(1)))
    }

    @After
    fun tearDown() = db.close()

    private fun item(id: Long, name: String, owner: Long, created: LocalDateTime) = ShoppingItem(
        id = id,
        ownerId = owner,
        groupId = groupId,
        name = name,
        amount = 1,
        price = null,
        creationTimestamp = created,
        priority = ShoppingItemPriority.Soon,
        check = null,
    )

    @Test
    fun uncheckedItemsIncludeMembersWithoutAccount() = runBlocking<Unit> {
        Sorting.entries.forEach { sorting ->
            val items = dao.findUnchecked(groupId, sorting).first()

            assertEquals("sorting=$sorting", setOf(1L, 2L), items.map { it.item.id }.toSet())
        }
    }

    @Test
    fun usernameSortingFallsBackToTheMemberName() = runBlocking<Unit> {
        val items = dao.findUnchecked(groupId, Sorting.Username).first()

        // Descending: "zoe" (username) before "adam" (member first name, no account).
        assertEquals(listOf("Milk", "Bread"), items.map { it.item.name })
    }

    @Test
    fun checkedItemsKeepTheirCheckingMember() = runBlocking<Unit> {
        val bread = dao.findUnchecked(groupId, Sorting.Name).first().first { it.item.id == 2L }.item
        dao.upsert(bread.copy(check = ShoppingItem.CheckoffState(checkingMemberId = adamMember)))

        val checked = dao.findChecked(groupId, Sorting.CreationDate).first()

        assertEquals(listOf(2L), checked.map { it.item.id })
        assertEquals(adamMember, checked.single().checkingMember?.id)
        assertEquals(
            listOf(1L),
            dao.findUnchecked(groupId, Sorting.CreationDate).first().map { it.item.id })
    }
}
