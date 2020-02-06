package com.mediapocket.android.view.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.mediapocket.android.R
import com.mediapocket.android.core.RxBus
import com.mediapocket.android.events.LoadNetworkItemsEvent
import com.mediapocket.android.model.Network
import com.mediapocket.android.utils.ViewUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView

/**
 * @author Vlad Namashko
 */
class NetworkListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<NetworkListAdapter.Holder>() {

    private var networks: List<Network>? = null

    fun load(networks : List<Network>) {
        this.networks = networks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(NetworkView().createView(AnkoContext.create(parent.context, parent)))
    }

    override fun getItemCount() : Int = networks?.size ?: 0

    override fun onBindViewHolder(holder: Holder, position: Int) {
        networks?.let {
            val network = it[position]
//            val text = holder.textView
//            text.text = network.title

            holder.bind(network)

//            val drawable = holder.textView.background

            // TODO
//            drawable.setColorFilter(text.resources.getColor(R.color.grey), PorterDuff.Mode.SRC_IN)

//            text.setOnClickListener {
//                RxBus.default.postEvent(LoadGenreItemsEvent(network.genreId))
//            }
        }
    }

    class NetworkView : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            return with(ui) {
                cardView {
                    lparams(width = matchParent, height = wrapContent)
                    preventCornerOverlap = false
                    elevation = dip(4).toFloat()
                    radius = dip(4).toFloat()

                    imageView {
                        id = R.id.icon
                    }.lparams(width = matchParent, height = wrapContent)
                }
            }
        }

    }

    class Holder(itemView: View) :  androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val logo : ImageView = itemView.find(R.id.icon)

        fun bind(network: Network) {
            val size = getSize()
            itemView.layoutParams.width = size
            itemView.layoutParams.height = size

            Glide.with(logo).load(network.logo).into(logo)

            itemView.setOnClickListener {
                RxBus.default.postEvent(LoadNetworkItemsEvent(network.id, network.title))
            }
        }

        protected fun getSize() = (ViewUtils.getRealScreenSize(itemView.context).x / 2.6 - 2 * PodcastListAdapter.ITEM_GAP).toInt()
    }
}