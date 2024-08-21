package com.example.calendartest

import android.content.Intent
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Defines the CalendarItemAdapter class, which extends RecyclerView.Adapter. The RecyclerView
// .Adapter will bind 'CalendarItem' data to the views in RecyclerView

//RecyclerView.Adapter<T> is a generic class that requires a type parameter 'T'. This represents
// the ViewHolder used to "hold" the views for each item in RecyclerView

//CalendarItemAdapter.ViewHolder - the 'Adapter' will use a custom 'ViewHolder'class, which is
// defined within the CalendarItemAdapter class.
class CalendarItemAdapter(): RecyclerView.Adapter<CalendarItemAdapter.ViewHolder>() {

    //Creates a mutable list of CalendarItem objects - these represent calendar entries
    var data: MutableList<CalendarItem> = mutableListOf()

    //initializes an empty mutable list to hold these objects, representing calendar entries
    fun clearData() {
        data.clear()
        //used to inform the adapter that the underlying data has been changed - refreshes the
        // dataset and re-binds all the views in RecyclerView.

        //this triggers UI updates, and forces it to rebind all visible views.
        notifyDataSetChanged()
    }

    //this function adds a new CalendarItem to the data list and then refreshes the RecyclerView
    fun pushData(calendarItem: CalendarItem) {
        data.add(calendarItem)
        notifyDataSetChanged()
    }

    //defines an inner 'ViewHolder' class which represents each item view in RecyclerView
    //declares a new class named 'ViewHolder' - takes a single parameter as the primary constructor
    //the : indicates that we are INHERITING from another class. ViewHolder is a SUBCLASS of the
    // RecyclerView.ViewHolder class, which takes in the 'view' parameter

    //this is responsible for holding the views associated with each individual item in
    // RecyclerView. when RecyclerView binds data to an item, it uses the ViewHolder to access
    // the views within that item (ex. TextView displays text)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemContainer: View = view.findViewById(R.id.item_container)
        val viewColor: View = view.findViewById(R.id.view_color)
        val textDisplayName: TextView = view.findViewById(R.id.text_display_name)
        val textAccountName: TextView = view.findViewById(R.id.text_account_name)
        val textAccountType: TextView = view.findViewById(R.id.text_account_type)
        val textVisible: TextView = view.findViewById(R.id.text_visible)
        val textSyncEvents: TextView = view.findViewById(R.id.text_sync_events)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarItemAdapter.ViewHolder {
        //LayoutInflater is responsible for turning an XML layout file into corresponding 'View'
        // objects in code. The 'context' is derived from the 'parent' which is the 'RecyclerView'

        //the inflate() method is used to create a 'View' objects from an XML layout resource. R
        // .layout.item_calendar is the layout resource ID for the item layout

        //the second parameter ('parent') is the ViewGroup (i.e. the RecyclerView) that will
        // contain the new view.

        //the third parameter 'false' means that you will attach the inflated View to the parent
        // later in the adapter's lifecycle.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent,
            false)

        //View is passed to the ViewHolder class, which creates a new instance
        return ViewHolder(view)
    }

    //this method binds the data from the 'data' list, and updates the views with the respective
    // CalendarItem data.
    override fun onBindViewHolder(holder: CalendarItemAdapter.ViewHolder, position: Int) {
        val datum = data[position]

        //sets a click listener on the itemContainer view
        holder.itemContainer.setOnClickListener {
            //an 'intent' is created, which is used to start a new activity or perform some
            // operation in the app

            //context is the environment in which the app is running. In this case, it is in the
            // context of the RecyclerView item.

            //holder.itemView refers to the root view of the calendar item. This is accessed
            // because it is part of the RecyclerView.ViewHolder class.

            //CalendarActivity::class.java specifies the activity that should be launched. In
            // this case, it is CalendarActivity
            val intent = Intent(holder.itemView.context, CalendarActivity::class.java).apply {
                //used to add extra data to the 'Intent'. This data is passed to the new activity
                // when it is launched. datum.id.toString() retrieves the unique id of the
                // selected calendar
                putExtra(CalendarActivity.EXTRA_CALENDAR_ID, datum.id.toString())
            }
            holder.itemView.context.startActivity(intent)
            //triggers the start of the CalendarActivity using the intent created above.
        }

        datum.color?.let {
            holder.viewColor.setBackgroundColor(it)
        }
        holder.textDisplayName.text = datum.displayName
        holder.textAccountName.text = datum.accountName
        holder.textAccountType.text = "(${datum.accountType})"
        holder.textVisible.text = datum.visible.toString()
        holder.textSyncEvents.text = datum.syncEvents.toString()
        Log.i("Calendar", "Calendar bound")
    }

    override fun getItemCount(): Int {
        return data.size
    }
}