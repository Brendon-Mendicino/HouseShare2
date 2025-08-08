package lol.terabrendon.houseshare2.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.data.entity.composite.CheckoffStateWithUser
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.mapper.CheckoffModelMapper
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.mapper.ShoppingItemModelMapper
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
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
}