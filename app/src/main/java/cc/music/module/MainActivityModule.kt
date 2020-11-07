package cc.music.module

import androidx.lifecycle.ViewModelProvider
import cc.music.BuildConfig
import cc.music.activity.MainActivity
import cc.music.manager.ApiManager
import cc.music.manager.MuzApiManager
import cc.music.model.AppViewModel
import cc.music.mvp.presenter.MainPresenter
import cc.music.mvp.presenter.MainPresenterImp
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
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

    @Provides
    fun provideMuzApiManager(manager: MuzApiManager): ApiManager = manager

    @Provides
    fun provideViewModel(owner: MainActivity) =
        ViewModelProvider(owner).get(AppViewModel::class.java)

    @Provides
    fun provideMainPresenter(presenter: MainPresenterImp): MainPresenter = presenter

    @Provides
    fun provideApiService(): MuzApiManager.APIService = Retrofit.Builder()
        .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
        .baseUrl(SERVER)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(MuzApiManager.APIService::class.java)

    @Provides
    fun provideDisposable() = CompositeDisposable()

    @Provides
    fun provideClientId(idIterator: Iterator<String>) = idIterator.next()
}