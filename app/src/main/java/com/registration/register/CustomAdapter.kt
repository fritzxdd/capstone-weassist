import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.registration.register.R

class CustomAdapter(
    private val context: Context,
    private val dataList: List<Quadruple<String, String, String, Int>>,
    private val itemClickListener: (Quadruple<String, String, String, Int>) -> Unit
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        private val idTextView: TextView = view.findViewById(R.id.idTextView)
        private val roleTextView: TextView = view.findViewById(R.id.roleTextView)
        private val imageView: ImageView = view.findViewById(R.id.imageView)

        fun bind(data: Quadruple<String, String, String, Int>) {
            nameTextView.text = data.first
            idTextView.text = data.second
            roleTextView.text = data.third
            imageView.setImageResource(data.fourth)

            // Set click listener using lambda passed from adapter constructor
            itemView.setOnClickListener {
                itemClickListener(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
