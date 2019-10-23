package muz.all.module

import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import muz.all.BuildConfig
import muz.all.manager.ApiManager
import muz.all.manager.MuzApiManager
import muz.all.mvp.presenter.MainPresenter
import muz.all.mvp.presenter.MainPresenterImp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@Module
class MainActivityModule {
    companion object {
        private const val SERVER = BuildConfig.SERVER
        private val TAG = MainActivityModule::class.java.simpleName
    }

    private var iterator: Iterator<String>? = null

    @Provides
    fun provideMuzApiManager(manager: MuzApiManager): ApiManager = manager

    @Provides
    fun provideMainPresenter(presenter: MainPresenterImp): MainPresenter = presenter

    @Provides
    fun provideApiService(): MuzApiManager.APIService = Retrofit.Builder()
        .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
        .baseUrl(SERVER)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(MuzApiManager.APIService::class.java)

    @Provides
    fun provideDisposable() = CompositeDisposable()

    @Provides
    fun provideIdIterator() = listOf(
        BuildConfig.CLIENT_ID_26,
        BuildConfig.CLIENT_ID_25,
        BuildConfig.CLIENT_ID_24,
        BuildConfig.CLIENT_ID_23,
        BuildConfig.CLIENT_ID_22,
        BuildConfig.CLIENT_ID_21,
        BuildConfig.CLIENT_ID_20,
        BuildConfig.CLIENT_ID_1,
        BuildConfig.CLIENT_ID_2,
        BuildConfig.CLIENT_ID_3,
        BuildConfig.CLIENT_ID_4,
        BuildConfig.CLIENT_ID_5,
        BuildConfig.CLIENT_ID_6,
        BuildConfig.CLIENT_ID_7,
        BuildConfig.CLIENT_ID_8,
        BuildConfig.CLIENT_ID_9,
        BuildConfig.CLIENT_ID_10,
        BuildConfig.CLIENT_ID_11,
        BuildConfig.CLIENT_ID_12,
        BuildConfig.CLIENT_ID_13,
        BuildConfig.CLIENT_ID_14,
        BuildConfig.CLIENT_ID_15,
        BuildConfig.CLIENT_ID_16,
        BuildConfig.CLIENT_ID_17,
        BuildConfig.CLIENT_ID_18,
        BuildConfig.CLIENT_ID_19
    ).iterator()

    @Provides
    fun provideClientId(idIterator: Iterator<String>) = idIterator.next()
}