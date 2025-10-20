package lol.terabrendon.houseshare2.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.data.remote.dto.ShoppingItemDto
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import lol.terabrendon.houseshare2.domain.mapper.GroupMapper
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.mapper.ShoppingMapper
import lol.terabrendon.houseshare2.domain.mapper.UserMapper
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {
    @Provides
    fun shoppingEntityToModel(impl: ShoppingMapper.EntityToModel): Mapper<ShoppingItemWithUser, ShoppingItemModel> =
        impl

    @Provides
    fun shoppingModelToDto(impl: ShoppingMapper.ModelToDto): Mapper<ShoppingItemInfoModel, ShoppingItemDto> =
        impl

    @Provides
    fun shoppingModelToEntity(impl: ShoppingMapper.ModelToEntity): Mapper<ShoppingItemInfoModel, ShoppingItem> =
        impl

    @Provides
    fun shoppingDtoToEntity(impl: ShoppingMapper.DtoToEntity): Mapper<ShoppingItemDto, ShoppingItem> =
        impl

    @Provides
    fun shoppingFormToModel(impl: ShoppingMapper.FormToModel): Mapper<ShoppingItemFormState, ShoppingItemInfoModel> =
        impl

    @Provides
    fun groupModelToDto(impl: GroupMapper.ModelToDto): Mapper<GroupModel, GroupDto> = impl

    @Provides
    fun groupDtoToEntity(impl: GroupMapper.DtoToEntity): Mapper<GroupDto, Group> = impl

    @Provides
    fun groupFormToModel(impl: GroupMapper.FormToModel): Mapper<GroupFormState, GroupModel> = impl

    @Provides
    fun groupEntityToModel(impl: GroupMapper.EntityToModel): Mapper<GroupWithUsers, GroupModel> =
        impl

    @Provides
    fun userDtoToEntity(impl: UserMapper.DtoToEntity): Mapper<UserDto, User> = impl

    @Provides
    fun userEntityToModel(impl: UserMapper.EntityToModel): Mapper<User, UserModel> = impl
}