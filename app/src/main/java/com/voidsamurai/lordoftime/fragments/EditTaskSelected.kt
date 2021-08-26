package com.voidsamurai.lordoftime.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentTaskEditBinding
import com.voidsamurai.lordoftime.fragments.adapters.ArrayColorAdapter
import layout.DataRowWithColor
import java.util.*


class EditTaskSelected : Fragment() ,DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{
    companion object{
        private lateinit var adapter: ArrayColorAdapter
    }
    private var _binding: FragmentTaskEditBinding?=null
    private val binding get()=_binding!!

    private var dateFormat:java.text.DateFormat= java.text.DateFormat.getDateInstance(
        java.text.DateFormat.LONG,
        Locale.getDefault()
    )

    private var data:DataRowWithColor? = null
    private lateinit var newDate: Calendar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentTaskEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColorSpinner()

        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))


        if(arguments!=null&&EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor!=null){
            data =EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor
            binding.deleteEditButton.visibility=View.VISIBLE
            binding.nameEdit.setText(data!!.name)
            binding.checkCategory.setSelection(adapter.getPosition(Pair(data!!.category,data!!.color)))
            binding.priorityEdit.setText(data!!.priority.toString())
            binding.dateEdit.setText(dateFormat.format(data!!.date.time))
            binding.durationEdit.setText(data!!.workingTime.toString())
            binding.hourEdit.setText(String.format("%s:%s",data!!.date.get(Calendar.HOUR),data!!.date.get(Calendar.MINUTE)))
            newDate= Calendar.getInstance()
            data!!.date.let {newDate.set(
                it.get(Calendar.YEAR),
                it.get(Calendar.MONTH),
                it.get(Calendar.DAY_OF_MONTH),
                it.get(Calendar.HOUR),
                it.get(Calendar.MINUTE),
                0
            )
                newDate.set(Calendar.MILLISECOND, 0)
            }
            binding.deleteEditButton.setOnClickListener{
                deleteRow(data!!)
                update()
                it.findNavController().navigateUp()
            }

            binding.saveEditButton.setOnClickListener{
                updateRow()
                update()
                it.findNavController().navigateUp()
            }
        }else{
            newDate= Calendar.getInstance()
            newDate.set(Calendar.SECOND, 0)
            newDate.set(Calendar.MILLISECOND, 0)
            binding.dateEdit.setText(dateFormat.format(newDate.time))
            binding.hourEdit.setText("${newDate.get(Calendar.HOUR)}:${newDate.get(Calendar.MINUTE)}")

            binding.deleteEditButton.visibility=View.GONE

            binding.saveEditButton.setOnClickListener{

                addRow(
                    (binding.checkCategory.selectedItem as Pair<*, *>).first.toString (),
                    binding.nameEdit.text.toString(),
                    newDate.time.time,
                    binding.durationEdit.text.toString(),
                    binding.priorityEdit.text.toString().toInt()
                )
                update()
                it.findNavController().navigateUp()
            }
        }

        binding.addColor.setOnClickListener{
            it?.findNavController()!!.navigate(R.id.action_editTask_to_colorsFragment2)
        }

        binding.dateEdit.setOnClickListener{
            val dp =
                DatePickerDialog(
                    requireContext(),
                    this,
                    newDate.get(Calendar.YEAR),
                    newDate.get(Calendar.MONTH),
                    newDate.get(Calendar.DAY_OF_MONTH)
                )
            dp.show()
        }

        binding.hourEdit.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(), this,
                newDate.get(Calendar.HOUR), newDate.get(Calendar.MINUTE), true
            )

            tp.show()
        }

        binding.cancelEditButton.setOnClickListener{
            it.findNavController().navigateUp()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
    private fun updateRow() {
        MainActivity.getDBOpenHelper().editTaskRow(
            data!!.id,
            (binding.checkCategory.selectedItem as Pair<*, *>).first.toString (),
            binding.nameEdit.text.toString(),
            newDate.time.time,
            binding.durationEdit.text.toString(),
            binding.priorityEdit.text.toString().toInt()
        )
        MainActivity.getDBOpenHelper().editOldstatRow(
            data!!.date.time.time,
            newDate.time.time,
            binding.durationEdit.text.toString()
        )
    }

    private fun addRow(
        category: String,
        name: String,
        startDateTime: Long,
        hours: String,
        priority: Int
    ) {
        MainActivity.getDBOpenHelper().addTaskRow(category, name, startDateTime, hours, priority)
        MainActivity.getDBOpenHelper().addOldstatRow(startDateTime,hours)
        update()
    }

    private fun deleteRow(dataRowWithColor: DataRowWithColor){
        MainActivity.getDBOpenHelper().deleteTaskRow(dataRowWithColor.id)
        MainActivity.getDBOpenHelper().deleteOldstatRow(dataRowWithColor.date.time.time)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        newDate.set(year, month, dayOfMonth)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        binding.dateEdit.setText(dateFormat.format(newDate.time))
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        newDate.set(Calendar.HOUR, hourOfDay)
        newDate.set(Calendar.MINUTE, minute)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        binding.hourEdit.setText((if (hourOfDay < 10) "0" else "") + hourOfDay.toString() + ":" + if (minute < 10) "0" else "" + minute.toString())
    }

    private fun update(){
        (context as MainActivity).getDataFromDB()
        EditList.update()
        HomeFragment.fillViewsWithDatabaseData()
    }
    private fun setColorSpinner(){
        val list: Array<String> = MainActivity.getColors().keys.toTypedArray()
        list.sort()

        adapter= ArrayColorAdapter(requireContext(),R.layout.color_edit_element,MainActivity.getColors().toList())
        adapter.setDropDownViewResource(R.layout.color_edit_element)
        binding.checkCategory.adapter = adapter
    }
}