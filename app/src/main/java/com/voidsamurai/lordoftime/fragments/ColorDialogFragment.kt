package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.slider.LightnessSlider
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R


class ColorDialogFragment(
    private var LayoutId: Int,
    private var dialogType: Int,
    private var oldCategory: String? = null,
    private var oldColor: String? = null
) : AppCompatDialogFragment() {
    companion object{
        const val SAVE=1
        const val EDIT=2
    }

    private lateinit var vieww: View
    private lateinit var colorPicker:ColorPickerView
    private lateinit var lightSlider:LightnessSlider


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        vieww= inflater.inflate(LayoutId, null)

        oldCategory?.let{ vieww.findViewById<TextView>(R.id.category_name).text=it}


        colorPicker=vieww.findViewById(R.id.color_picker_view)
        lightSlider=vieww.findViewById(R.id.v_lightness_slider)
        builder.setView(vieww)
            .setNegativeButton("Anuluj") { _, _ ->

            }
        vieww.findViewById<ImageButton>(R.id.delete_color).setOnClickListener {
            MainActivity.getDBOpenHelper().deleteColorRow(getName())
            update()
            Log.v("test",""+ colorPicker.selectedColor)
            dismiss()
        }
        if(dialogType==1){

            colorPicker.addOnColorSelectedListener {

                setColorToImageView(vieww, R.id.last_color, R.drawable.ic_kolo_l, it)
                setColorToImageView(vieww, R.id.new_color, R.drawable.ic_kolo_r, it)
            }
            lightSlider.setOnValueChangedListener {
                setColorToImageView(vieww, R.id.last_color, R.drawable.ic_kolo_l, colorPicker.selectedColor)
                setColorToImageView(vieww, R.id.new_color, R.drawable.ic_kolo_r, colorPicker.selectedColor)
            }
            builder.setPositiveButton("Zapisz") { _, _ ->
                MainActivity.getDBOpenHelper().addColorRow(getName(), getColor())
                update()

            }
        }
        else if(dialogType==2){
            colorPicker.addOnColorSelectedListener {
                setColorToImageView(vieww, R.id.new_color, R.drawable.ic_kolo_r, it)
            }
            lightSlider.setOnValueChangedListener {
                setColorToImageView(vieww, R.id.new_color, R.drawable.ic_kolo_r, colorPicker.selectedColor)
            }
            builder.setPositiveButton("Zapisz") { _, _ ->
                if (oldCategory != null && oldColor != null)
                    MainActivity.getDBOpenHelper().editColorRow(oldCategory, getName(), getColor())
                update()
            }
        }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        oldColor?.let{
            if(dialogType==2){

                setColorToImageView(vieww, R.id.last_color, R.drawable.ic_kolo_l, parseColor(oldColor))
                setColorToImageView(vieww, R.id.new_color, R.drawable.ic_kolo_r, parseColor(oldColor))
            }
        }

    }

    private fun getName():String= vieww.findViewById<TextView>(R.id.category_name).text.toString()
    private fun getColor():String=String.format(
        "#%06X", 0xFFFFFF and
                colorPicker.selectedColor
    )
    private fun update(){
        (context as MainActivity).getDataFromDB()
        ColorsFragment.fillEditList(MainActivity.getColors())}

    private fun getColoredCircle(id: Int, color: Int):Drawable{
        val unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), id)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }
    private fun parseColor(oldColor: String?):Int{
        val color = Color.parseColor(oldColor)
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color shr 0 and 0xFF
        return Color.argb(255,r,g,b)
    }
    private fun setColorToImageView(view:View,elementId:Int,drawableId:Int,color:Int){
        view.findViewById<ImageView>(elementId).setImageDrawable(
            getColoredCircle(
                drawableId,
                color
            )
        )
    }
}