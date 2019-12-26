package com.mediapocket.android

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mediapocket.android.audio.AudioVolumeObserver
import com.mediapocket.android.audio.OnAudioVolumeChangedListener
import com.mediapocket.android.core.DependencyLocator
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.core.download.PodcastDownloadManager
import com.mediapocket.android.dao.model.PodcastEpisodeItem
import com.mediapocket.android.events.*
import com.mediapocket.android.fragments.*
import com.mediapocket.android.fragments.transition.DetailsTransition
import com.mediapocket.android.journeys.discover.DiscoverFragment
import com.mediapocket.android.journeys.discover.PodcastSearchFragment
import com.mediapocket.android.utils.ViewUtils
import com.mediapocket.android.view.PodcastPlaybackCompatView
import com.mediapocket.android.view.PodcastPlaybackExpandedView
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    lateinit var toolbar: Toolbar

    private val disposable = CompositeDisposable()

//    private var mMediaBrowser: MediaBrowserCompat? = null
//
//    private lateinit var mediaController: MediaControllerCompat

    private lateinit var mediaConnection : MediaSessionConnection

    lateinit var playbackControl: PodcastPlaybackCompatView
    lateinit var playbackExpandedControl: PodcastPlaybackExpandedView

    @set:Inject
    lateinit var downloadManager: PodcastDownloadManager

    private lateinit var audioVolumeObserver : AudioVolumeObserver

    private val volumeChangeListener = object: OnAudioVolumeChangedListener {
        override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
            RxBus.default.postEvent(VolumeLevelKeyEvent(currentVolume))
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_discover -> {
                val fragment = DiscoverFragment.newInstance()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                onFragmentChanged(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_subscriptions -> {
                val fragment = PodcastSubscriptionFragment.newInstance()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                onFragmentChanged(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_downloaded -> {
                val fragment = EpisodesFragment.newInstance()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                onFragmentChanged(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DependencyLocator.initInstance(this)

        AndroidInjection.inject(this)

        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mediaConnection = MediaSessionConnection.getInstance(applicationContext)
        volumeControlStream = AudioManager.STREAM_MUSIC

        playbackControl = findViewById(R.id.playback_compact_view)
        playbackExpandedControl = findViewById(R.id.playback_expanded_view)

        disposable.add(mediaConnection.connected(Consumer {
            playbackControl.initWithMediaConnection(mediaConnection)
            playbackExpandedControl.initWithMediaConnection(mediaConnection)

            playbackExpandedControl.setDisposable(disposable)
        }, Consumer {
            it.printStackTrace()
        }))

        audioVolumeObserver = AudioVolumeObserver(this)
        audioVolumeObserver.register(AudioManager.STREAM_MUSIC, volumeChangeListener)

        supportFragmentManager.beginTransaction()
                .add(R.id.frame, DiscoverFragment.newInstance(), DiscoverFragment.TAG)
                .addToBackStack(DiscoverFragment.TAG)
                .commit()

        disposable.add(RxBus.default.observerFor(PodcastSelectedEvent::class.java).subscribe { event ->
            val details = PodcastDetailsFragment.newInstance(event.podcast)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

//                val transition = DetailsTransition()
//                transition.duration = 3000

                details.sharedElementEnterTransition = DetailsTransition()
                details.enterTransition = Fade()

//                details.sharedElementReturnTransition = transition
//                details.returnTransition = Fade()

//                details.returnTransition = Fade()
//                supportFragmentManager.findFragmentById(R.id.frame).exitTransition = Fade()
                details.sharedElementReturnTransition = DetailsTransition()
            }

            supportActionBar?.setDisplayShowHomeEnabled(false)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, details, PodcastDetailsFragment.TAG)
                    .addSharedElement(event.logo, "logo")
                    .addToBackStack(PodcastDetailsFragment.TAG)
                    .commit()
        })

        disposable.add(RxBus.default.observerFor(OpenSearchEvent::class.java).subscribe {
            val fragment = PodcastSearchFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, fragment)
                    .addToBackStack(PodcastSearchFragment.TAG)
                    .commit()
//            onFragmentChanged(fragment)
        })

        disposable.add(RxBus.default.observerFor(CloseSearchEvent::class.java).subscribe {

            //            val fragment = DiscoverFragment.newInstance()
//            supportFragmentManager.beginTransaction()
//                    .replace(R.id.frame, fragment)
//                    .addToBackStack(DiscoverFragment.TAG)
//                    .commit()
//            onFragmentChanged(fragment)

            supportFragmentManager.popBackStackImmediate()
//            onFragmentChanged(fragment)
        })

        disposable.add(RxBus.default.observerFor(PopBackStackEvent::class.java).subscribe {
            supportFragmentManager.popBackStack()
        })

        disposable.add(RxBus.default.observerFor(SwitchPodcastPlayerModeEvent::class.java).subscribe {
            switchPodcastViewVisibility(it.action == SwitchPodcastPlayerModeEvent.OPEN)
        })

        disposable.add(RxBus.default.observerFor(LoadGenreItemsEvent::class.java).subscribe {
            slideFragment(GenrePodcastsFragment.newInstance(it.genreId), GenrePodcastsFragment.TAG)
        })

        disposable.add(RxBus.default.observerFor(LoadNetworkItemsEvent::class.java).subscribe {
            slideFragment(NetworkPodcastFragment.newInstance(it.networkId, it.networkTitle), NetworkPodcastFragment.TAG)
        })

        disposable.add(RxBus.default.observerFor(ChangeStatusBarColorEvent::class.java).subscribe {
            window.statusBarColor = it.color
        })

        disposable.add(RxBus.default.observerFor(StopPlaybackEvent::class.java).subscribe {
            playbackControl.visibility = View.GONE
        })

        val notificationBuilder = NotificationBuilder(this)
        disposable.add(downloadManager.subscribeForAllActiveDownloads(Consumer {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (it.isEmpty()) {
                manager.cancel(NotificationBuilder.DOWNLOADING_NOTIFICATION)
            } else {
                manager.notify(NotificationBuilder.DOWNLOADING_NOTIFICATION, notificationBuilder.buildDownloadNotification(it))
            }
        }))

        disposable.add(downloadManager.subscribeForDownloads(Consumer { download ->
            if (download.state == PodcastEpisodeItem.STATE_WAITING_FOR_NETWORK) {
                Toast.makeText(DependencyLocator.getInstance().context, R.string.waiting_for_network, Toast.LENGTH_LONG).show()
            } else if (download.state == PodcastEpisodeItem.STATE_ADDED) {
                Toast.makeText(DependencyLocator.getInstance().context,
                        resources.getString(R.string.downloading_episode, download.title), Toast.LENGTH_LONG).show()
            }
        }))

        var currentMediaId: String? = null
        disposable.add(RxBus.default.observerFor(PlayPodcastEvent::class.java).subscribe { event ->
            if (currentMediaId != event.item.mediaId) {
                mediaConnection.mediaBrowser.subscribe(event.item.mediaId, object : MediaBrowserCompat.SubscriptionCallback() {

                    override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
                        currentMediaId = parentId
                        mediaConnection.mediaController.transportControls.playFromMediaId(event.item.link, null)
                    }
                })
            } else {
                mediaConnection.mediaController.transportControls.playFromMediaId(event.item.link, null)
            }

        })

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.frame)
            if (fragment != null) {
                onFragmentChanged(fragment as BaseFragment)
            } else {
                finish()
            }
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun slideFragment(fragment: androidx.fragment.app.Fragment, tag: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.enterTransition = Slide(Gravity.RIGHT)
            fragment.exitTransition = Slide(Gravity.LEFT)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .addToBackStack(tag)
                .commit()
    }

    private fun switchPodcastViewVisibility(show: Boolean) {
        val property = "translationY"
        val height = if (playbackExpandedControl.height == 0) ViewUtils.getRealScreenSize(baseContext).y.toFloat() else playbackExpandedControl.height.toFloat()

        if (show) {
            playbackExpandedControl.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(playbackExpandedControl, property, height, 0f).apply {
                duration = 500
                start()
            }
        } else {
            val animator = ObjectAnimator.ofFloat(playbackExpandedControl, property, 0f, height).apply {
                duration = 500
                start()
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    playbackExpandedControl.visibility = View.GONE
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (playbackExpandedControl.visibility == View.VISIBLE) {
            val viewRect = Rect()
            playbackExpandedControl.getGlobalVisibleRect(viewRect)
            if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                switchPodcastViewVisibility(false)
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    public override fun onStart() {
        super.onStart()
//        mMediaBrowser?.connect()
    }

    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
//        if (mediaController != null) {
//            mediaController.unregisterCallback(mMediaControllerCallback)
//        }
//        mMediaBrowser?.disconnect()

    }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        return when (event.keyCode) {
//            KeyEvent.KEYCODE_VOLUME_UP -> {
//                if (event.action == KeyEvent.ACTION_DOWN) {
//                    RxBus.default.postEvent(VolumeLevelKeyEvent(1))
//                }
//                true
//            }
//            KeyEvent.KEYCODE_VOLUME_DOWN -> {
//                if (event.action == KeyEvent.ACTION_DOWN) {
//                    RxBus.default.postEvent(VolumeLevelKeyEvent(-1))
//                }
//                true
//            }
//            else -> super.dispatchKeyEvent(event)
//        }
//    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            RxBus.default.postEvent(VolumeLevelKeyEvent(-1))
//            return true
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            RxBus.default.postEvent(VolumeLevelKeyEvent(1))
//            return true
//        }
//
//        return super.onKeyDown(keyCode, event)
//    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame)
        if (fragment != null && fragment is PodcastDetailsFragment) {
            val details = PodcastDetailsFragment.newInstance(fragment.podcast!!)
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, details, PodcastDetailsFragment.TAG)
//                    .add(R.id.frame, details)
//                    .hide(supportFragmentManager.findFragmentById(R.id.frame) as BaseFragment)
//                    .addSharedElement(fragment.getSha, "logo")
                    .addToBackStack(PodcastDetailsFragment.TAG)
                    .commit()
        }
    }


    override fun onBackPressed() {
        when {
            playbackExpandedControl.visibility == View.VISIBLE -> switchPodcastViewVisibility(false)
            supportFragmentManager.backStackEntryCount == 1 -> finish()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackControl.detachMediaConnectionCallback()
        disposable.clear()
    }

    private fun onFragmentChanged(top: BaseFragment) {
        title = top.getTitle()
        navigation.visibility = if (top.hasNavigation()) View.VISIBLE else View.GONE
        toolbar.visibility = if (top.hasNavigation()) View.VISIBLE else View.GONE
        setBackVisibility(top.hasBackNavigation())
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    private fun setBackVisibility(visible: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(visible)
        supportActionBar?.setDisplayShowHomeEnabled(visible)
    }
}
