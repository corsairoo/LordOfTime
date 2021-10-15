package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.fragments.adapters.ArrayColorAdapter
import java.util.*


class EditTaskDialog(
    private var LayoutId: Int,
    private val mode:MODE,
    private val startTime:Calendar?=null,
    private val margin:Float?=null,
    private val id:Int?=null
) : DialogFragment() {

    companion object{
        enum class MODE(type:Int){
            SAVE(1),
            EDIT(2)
        }
    }
    private var fragment:Fragment?=null
    private lateinit var adapter: ArrayColorAdapter
    private lateinit var nameEdit:EditText
    private lateinit var category:Spinner
    private lateinit var priority:EditText
    private lateinit var startHour:EditText
    private lateinit var endHour:EditText
    private lateinit var durationEdit:EditText
    private lateinit var addColorB:ImageButton
    private lateinit var deleteButton:Button
    private lateinit var cancelButton:Button
    private lateinit var saveButton:Button
    private lateinit var startHourCalendar:Calendar
    private lateinit var endHourCalendar:Calendar
    private var dataRow:DataRowWithColor? = null

    private lateinit var contentView: View

    fun setFrag(frag:Fragment){fragment=frag}
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(LayoutId, null)
        nameEdit =contentView.findViewById(R.id.name_edit)
        category=contentView.findViewById(R.id.check_category)
        priority=contentView.findViewById(R.id.priority_edit)
        startHour=contentView.findViewById(R.id.hour_edit)
        endHour=contentView.findViewById(R.id.hour_to_edit)
        durationEdit=contentView.findViewById(R.id.duration_edit)
        addColorB=contentView.findViewById(R.id.add_color)
        deleteButton=contentView.findViewById(R.id.delete_edit_button)
        cancelButton=contentView.findViewById(R.id.cancel)
        saveButton=contentView.findViewById(R.id.save)

        setColorSpinner()
        if(mode==MODE.SAVE&&startTime!=null){
            startHourCalendar=startTime.clone() as Calendar
            endHourCalendar=startTime.clone() as Calendar
            val time=String.format("%02d:%02d", startHourCalendar.get(Calendar.HOUR_OF_DAY),startHourCalendar.get(Calendar.MINUTE))
            startHour.setText(time)
            endHour.setText(time)
        }else{
            startHourCalendar= Calendar.getInstance()
            endHourCalendar= Calendar.getInstance()
            dataRow=(activity as MainActivity).getDBOpenHelper().getTaskRow(id!!)
            if(dataRow!=null){
                startHourCalendar= dataRow!!.date.clone() as Calendar
                val cal  =dataRow!!.date.clone() as Calendar
                val baseTime=dataRow!!.workingTime
                cal.add(Calendar.HOUR_OF_DAY,(baseTime/1).toInt())
                cal.add(Calendar.MINUTE,((baseTime%1)*60).toInt())


                endHourCalendar=cal.clone() as Calendar
                startHour.setText(String.format("%02d:%02d", startHourCalendar.get(Calendar.HOUR_OF_DAY),startHourCalendar.get(Calendar.MINUTE)))
                endHour.setText(String.format("%02d:%02d", endHourCalendar.get(Calendar.HOUR_OF_DAY),endHourCalendar.get(Calendar.MINUTE)))
                nameEdit.setText(dataRow!!.name)
                durationEdit.setText(dataRow!!.workingTime.toString())
                priority.setText(dataRow!!.priority.toString())
                category.setSelection(adapter.getPosition(Pair(dataRow!!.category,dataRow!!.color)))

            }

        }


        super.onCreateDialog(savedInstanceState)



        builder.setView(contentView)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            if(mode==MODE.SAVE){
                val color=resources.getColor(R.color.blue_gray,null)
                var canSave=true

                    if(nameEdit.text.isNullOrEmpty()){
                        nameEdit.backgroundTintList= ColorStateList.valueOf(Color.RED)
                        canSave=false
                    }else
                        nameEdit.backgroundTintList= ColorStateList.valueOf(color)

                    if(priority.text.isNullOrEmpty()){
                        priority.backgroundTintList= ColorStateList.valueOf(Color.RED)
                        canSave=false
                    }else
                        priority.backgroundTintList= ColorStateList.valueOf(color)

                if(canSave) {
                        if(durationEdit.text.isNullOrEmpty())
                            setDuration()
                    val id=addRow(
                        (category.selectedItem as Pair<*, *>).first.toString (),
                        nameEdit.text.toString(),
                        startTime!!.time.time,
                        durationEdit.text.toString(),
                        priority.text.toString().toInt()
                    ).toInt()

                    if(id!=-1) {
                        val _category = (category.selectedItem as Pair<*, *>).first.toString()
                        val drwc = DataRowWithColor(
                            name = nameEdit.text.toString(),
                            category = _category,
                            date = startHourCalendar,
                            workingTime = durationEdit.text.toString().toFloat(),
                            currentWorkingTime = 0f,
                            outdated = startTime.time.time < Calendar.getInstance().time.time,
                            color = (activity as MainActivity).getColors().value!![_category]!!,
                            priority = priority.text.toString().toInt(),
                            id =id

                        )
                        (fragment as CalendarDayEdit).addElement(
                            drwc,
                            margin?.toInt() ?: 1,
                            durationEdit.text.toString().toFloat()
                        )
                    }


                    dismiss()
                }

            }else{

                updateRow((activity as MainActivity).getDBOpenHelper().getTaskRow(id!!))
                dismiss()
            }

        }
        addColorB.setOnClickListener {
                it?.findNavController()!!.navigate(R.id.action_editTaskSelected_to_colorsFragment)

        }


        startHourCalendar.set(Calendar.SECOND, 0)                                                 //set hours from prev frag
        startHourCalendar.set(Calendar.MILLISECOND, 0)

        endHourCalendar.set(Calendar.SECOND, 0)
        endHourCalendar.set(Calendar.MILLISECOND, 0)

        startHour.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    startHourCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    startHourCalendar.set(Calendar.MINUTE, minute)
                    startHourCalendar.set(Calendar.SECOND, 0)
                    startHourCalendar.set(Calendar.MILLISECOND, 0)
                    startHour.setText(String.format("%02d:%02d", hourOfDay,minute))//(if (hourOfDay < 10) "0" else "") + hourOfDay.toString() + ":" + if (minute < 10) "0" else "" + minute.toString())

                },
                startHourCalendar.get(Calendar.HOUR_OF_DAY),
                startHourCalendar.get(Calendar.MINUTE),
                true
            )

            tp.show()
        }
        endHour.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(),
                { view, hourOfDay, minute ->
                    endHourCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    endHourCalendar.set(Calendar.MINUTE, minute)
                    endHourCalendar.set(Calendar.SECOND, 0)
                    endHourCalendar.set(Calendar.MILLISECOND, 0)
                    endHourCalendar.set(Calendar.MILLISECOND, 0)
                    endHour.setText(String.format("%02d:%02d", hourOfDay,minute))
                    setDuration()
                },
                endHourCalendar.get(Calendar.HOUR_OF_DAY),
                endHourCalendar.get(Calendar.MINUTE),
                true
            )
            tp.show()
        }



        durationEdit.addTextChangedListener{
            if(it!!.length>0) {

                val dur = (60f * it.toString().toFloat()).toInt()
                val minute = dur % 60
                val hour = (dur - minute) / 60
                endHourCalendar.set(Calendar.HOUR_OF_DAY,hour+startHourCalendar.get(Calendar.HOUR_OF_DAY))
                endHourCalendar.set(Calendar.MINUTE,minute+startHourCalendar.get(Calendar.MINUTE))
                endHour.setText(String.format("%02d:%02d", endHourCalendar.get(Calendar.HOUR_OF_DAY), endHourCalendar.get(Calendar.MINUTE)))
            }
        }
        if(mode==MODE.EDIT) {
            deleteButton.setOnClickListener {
                deleteRow(id!!)
                (fragment as CalendarDayEdit).deleteElement(id)
                dismiss()
            }
        }else{
            deleteButton.visibility=View.GONE
        }
        addColorB.setOnClickListener {

        }



        return builder.create()
    }
    fun setDuration(){
        val hoursInFloat=endHourCalendar.get(Calendar.HOUR_OF_DAY)-startHourCalendar.get(Calendar.HOUR_OF_DAY)+((endHourCalendar.get(Calendar.MINUTE)-startHourCalendar.get(Calendar.MINUTE)).toFloat()/60f)
        durationEdit.setText(hoursInFloat.toString())
    }
    override fun onResume() {
        super.onResume()

    }

    private fun setColorSpinner(){
        val list: Array<String> = (activity as MainActivity).getColors().value!!.keys.toTypedArray()
        list.sort()

        adapter= ArrayColorAdapter(requireContext(),R.layout.color_edit_element,(activity as MainActivity).getColors().value!!.toList())
        adapter.setDropDownViewResource(R.layout.color_edit_element)
        category.adapter = adapter
    }


    private fun updateRow(data:DataRowWithColor) {
        if(durationEdit.text.isNullOrEmpty())
            setDuration()

        (fragment as CalendarDayEdit).editElement(
            DataRowWithColor(
                data.id,
                (category.selectedItem as Pair<*, *>).first.toString (),
                nameEdit.text.toString(),
                startTime!!,
                (durationEdit.text.toString().toFloat()*3600),
                priority.text.toString().toInt(),
                0f,
                (category.selectedItem as Pair<*, *>).second.toString (),
                null
                )
        )

        (activity as MainActivity).getDBOpenHelper().editTaskRow(
            data.id,
            (category.selectedItem as Pair<*, *>).first.toString (),
            nameEdit.text.toString(),
            startTime.time.time,
            (durationEdit.text.toString().toFloat()*3600).toInt(),
            priority.text.toString().toInt(),
            0)
        (activity as MainActivity).tasks.add  ( id=data.id,
            category = (category.selectedItem as Pair<*, *>).first.toString (),
            name = nameEdit.text.toString(),
            dateTime = startTime.time.time,
            workingTime = (durationEdit.text.toString().toFloat()*3600).toInt(),
            priority = priority.text.toString().toInt(),
            currentWorkingTime = 0)
        (context as MainActivity).getDataFromDB()

    }

    private fun addRow(
        category: String,
        name: String,
        startDateTime: Long,
        hours: String,
        priority: Int
    ):Long {
        var dur = hours.toFloat()*3600

        margin?.let{
            val d=(fragment as CalendarDayEdit).getMaxDur(it.toInt(), (fragment as CalendarDayEdit).getHeight(hours.toFloat()))
           dur= d*3600
            durationEdit.setText(d.toString())
        }
        val id=(activity as MainActivity).getDBOpenHelper().addTaskRow(category, name, startDateTime,dur.toInt(), priority,0)
        if(id!=-1L)
            (activity as MainActivity).tasks.add  ( id=id.toInt(),category = category, name=name, dateTime = startDateTime, workingTime =dur.toInt(), priority = priority,currentWorkingTime = 0)
        (context as MainActivity).getDataFromDB()
        return id
    }
    private fun deleteRow(
        id:Int
    ):Int {
        (activity as MainActivity).getDBOpenHelper().deleteTaskRow(id)
        (activity as MainActivity).tasks.delete (id.toString())
        (context as MainActivity).getDataFromDB()
        return id
    }


}