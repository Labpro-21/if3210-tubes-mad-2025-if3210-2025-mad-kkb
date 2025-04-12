import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kkb.purrytify.R
import com.kkb.purrytify.data.model.Song
import com.bumptech.glide.Glide
import com.kkb.purrytify.UserSong

class SongAdapter(
    private var songs: List<UserSong>,
    private val onClick: (UserSong) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cover: ImageView = view.findViewById(R.id.image_cover)
        private val title: TextView = view.findViewById(R.id.text_title)
        private val artist: TextView = view.findViewById(R.id.text_artist)

        fun bind(song: UserSong) {
            title.text = song.title
            artist.text = song.artist

            Glide.with(cover.context)
                .load(song.coverPath)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(cover)

            itemView.setOnClickListener { onClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    fun updateList(newList: List<UserSong>) {
        songs = newList
        notifyDataSetChanged()
    }
}
