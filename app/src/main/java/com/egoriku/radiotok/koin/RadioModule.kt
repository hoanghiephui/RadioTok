package com.egoriku.radiotok.koin

import com.egoriku.radiotok.common.provider.IDrawableProvider
import com.egoriku.radiotok.common.provider.IStringResourceProvider
import com.egoriku.radiotok.data.Api
import com.egoriku.radiotok.data.datasource.RadioServerDataSource
import com.egoriku.radiotok.data.datasource.StationsDataSource
import com.egoriku.radiotok.data.repository.RadioFetchNetworkRepository
import com.egoriku.radiotok.domain.common.internal.DrawableProvider
import com.egoriku.radiotok.domain.common.internal.StringResourceProvider
import com.egoriku.radiotok.domain.datasource.IRadioServerDataSource
import com.egoriku.radiotok.domain.datasource.IStationsDataSource
import com.egoriku.radiotok.domain.mediator.RadioCacheMediator
import com.egoriku.radiotok.domain.repository.IRadioFetchNetworkRepository
import com.egoriku.radiotok.domain.usecase.IRadioCacheUseCase
import com.egoriku.radiotok.domain.usecase.RadioCacheUseCase
import com.egoriku.radiotok.presentation.IMusicServiceConnection
import com.egoriku.radiotok.presentation.MainActivity
import com.egoriku.radiotok.presentation.RadioServiceConnection
import com.egoriku.radiotok.presentation.RadioViewModel
import com.egoriku.radiotok.radioplayer.data.mediator.IRadioCacheMediator
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appScope = module {
    single<IDrawableProvider> { DrawableProvider(context = androidContext()) }
    single<IStringResourceProvider> { StringResourceProvider(context = androidContext()) }
}

val radioModule = module {
    factory<IRadioServerDataSource> { RadioServerDataSource() }
    factory<IStationsDataSource> { StationsDataSource(api = get()) }

    factory<IRadioFetchNetworkRepository> {
        RadioFetchNetworkRepository(
            radioServerDataSource = get(),
            stationsDataSource = get()
        )
    }

    factory<IRadioCacheUseCase> {
        RadioCacheUseCase(
            radioTokDb = get(),
            radioFetchNetworkRepository = get()
        )
    }

    single<IRadioCacheMediator> {
        RadioCacheMediator(
            radioCacheUseCase = get(),
            mediaItemRepository = get(),
            currentRadioQueueHolder = get()
        )
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<Api> {
        get<Retrofit>().create(Api::class.java)
    }

    single<IMusicServiceConnection> {
        RadioServiceConnection(context = androidContext())
    }

    scope<MainActivity> {
        viewModel {
            RadioViewModel(serviceConnection = get())
        }
    }
}