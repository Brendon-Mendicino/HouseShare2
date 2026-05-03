package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class HomepageNavigation : MainNavigation() {
    /** Cleaning **/
    @Serializable
    data object Cleaning : HomepageNavigation()

    /** Shopping **/
    @Serializable
    data object Shopping : HomepageNavigation()

    @Serializable
    data object ShoppingForm : HomepageNavigation()

    @Serializable
    data class ShoppingItem(val shoppingItemId: Long) : HomepageNavigation()

    /** Billing **/
    @Serializable
    data object Billing : HomepageNavigation()

    @Serializable
    data object ExpenseForm : HomepageNavigation()

    /** Groups **/
    @Serializable
    data object Groups : HomepageNavigation()

    @Serializable
    data class GroupInfo(val groupId: Long) : HomepageNavigation()

    @Serializable
    data object GroupUsersForm : HomepageNavigation()

    @Serializable
    data class GroupInfoForm(val groupId: Long?) : HomepageNavigation()

    /** Profile **/
    @Serializable
    data object UserProfile : HomepageNavigation()

    /** Settings **/
    @Serializable
    data object Settings : HomepageNavigation()
}