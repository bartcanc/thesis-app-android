import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thesisapp.R

data class Workout(
    val workoutId: Int,
    val workoutType: String,
    val duration: Int,
    val distance: Double,
    val caloriesBurned: Double,
    val date: String,
    val avgSteps: Int,
    val avgHeartrate: Double
)


class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onItemClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_item, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.tvWorkoutType.text = workout.workoutType
        holder.tvDate.text = workout.date

        holder.itemView.setOnClickListener { onItemClick(workout) }
    }

    override fun getItemCount(): Int = workouts.size
}



