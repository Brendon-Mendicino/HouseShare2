package lol.terabrendon.houseshare2.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.data.dto.GroupDto
import lol.terabrendon.houseshare2.data.dto.UserDto
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.entity.composite.CheckoffStateWithUser
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.mapper.CheckoffModelMapper
import lol.terabrendon.houseshare2.domain.mapper.GroupMapper
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.mapper.ShoppingItemModelMapper
import lol.terabrendon.houseshare2.domain.mapper.UserMapper
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel

@Module
@InstallIn(SingletonComponent::class)
class MapperModule {
    @Provides
    fun checkoffModelMapper(impl: CheckoffModelMapper): Mapper<CheckoffStateWithUser, CheckoffStateModel> =
        impl

    @Provides
    fun shoppingItemModelMapper(impl: ShoppingItemModelMapper): Mapper<ShoppingItemWithUser, ShoppingItemModel> =
        impl

    @Provides
    fun groupModelToDto(impl: GroupMapper.ModelToDto): Mapper<GroupModel, GroupDto> = impl

    @Provides
    fun groupDtoToEntity(impl: GroupMapper.DtoToEntity): Mapper<GroupDto, Group> = impl

    @Provides
    fun userDtoToEntity(impl: UserMapper.DtoToEntity): Mapper<UserDto, User> = impl
}