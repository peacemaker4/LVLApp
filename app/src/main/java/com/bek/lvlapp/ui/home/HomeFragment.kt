package com.bek.lvlapp.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bek.lvlapp.R
import com.bek.lvlapp.databinding.FragmentHomeBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.models.Quote
import com.daimajia.slider.library.Animations.DescriptionAnimation
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private var authManager = AuthManager()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var quote_1: Quote
    private lateinit var quote_1_textView: TextView

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "quotes"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Home"
        setHasOptionsMenu(true)

        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        quote_1_textView = binding.quote1

        if(quote_1_textView.text.isNotEmpty()){
            binding.progressBar.stop()
            binding.progressBar.visibility = View.GONE
        }
        else
            binding.progressBar.start()

        quote_1 = Quote()

        val quoteListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(quote_1.last_updated == null){
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext()) ?: return
                    val quote_update_time = sharedPref.getString(getString(R.string.pref_quote_update), resources.getString(R.string.def_quote_update))

                    for (quoteSnapshot in dataSnapshot.children) {
                        var quote = quoteSnapshot.getValue<Quote>()!!
                        quote_1 = Quote(quote.author, quote.quote, quote.last_updated)
                    }
                    if(_binding != null) {
                        if(quote_1.last_updated != null){
                            var last_upd = LocalDateTime.parse(quote_1.last_updated)
                            if(last_upd.isAfter(LocalDateTime.now().minusMinutes(quote_update_time!!.toLong()))){
                                binding.progressBar.stop()
                                binding.progressBar.visibility = View.GONE
                                quote_1_textView.text = "\"" + quote_1.quote + "\" - " + quote_1.author
                                binding.timeText.text = last_upd.format(DateTimeFormatter.ofPattern("HH:mm"))

                                binding.timeText.visibility = View.VISIBLE
                            }
                            else{
                                getQuote()
                            }
                        }
                        else{
                            getQuote()
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).addValueEventListener(quoteListener)
            quote_1_textView.setOnLongClickListener {
                if(quote_1_textView.text.isNotEmpty()){

                    val clipboard: ClipboardManager? =
                        context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("Quote", quote_1_textView.text)
                    clipboard!!.setPrimaryClip(clip)

                    val toast = SuperActivityToast.create(requireActivity(), Style(), Style.TYPE_STANDARD)
                        .setText("Copied")
                        .setDuration(Style.DURATION_VERY_SHORT)
                        .setFrame(Style.FRAME_KITKAT)
                        .setColor(resources.getColor(R.color.dark_50))
                        .setIconResource(R.drawable.ic_icon_copy)
                        .setHeight(88)
                        .setAnimations(Style.ANIMATIONS_SCALE)

                    toast.show()

                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }

        binding.refreshLayout.setColorSchemeColors(resources.getColor(R.color.main))

        binding.refreshLayout.setOnRefreshListener {
            if(!binding.progressBar.isStart){
                quote_1_textView.text = ""
                binding.timeText.text = ""
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.start()
                if(quote_1.last_updated != null){
                    var last_upd = LocalDateTime.parse(quote_1.last_updated)

                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val quote_update_time = sharedPref.getString(getString(R.string.pref_quote_update), resources.getString(R.string.def_quote_update))

                    if(last_upd.isAfter(LocalDateTime.now().minusMinutes(quote_update_time!!.toLong()))){
                        binding.progressBar.stop()
                        binding.progressBar.visibility = View.GONE
                        quote_1_textView.text = "\"" + quote_1.quote + "\" - " + quote_1.author
                        binding.timeText.text = last_upd.format(DateTimeFormatter.ofPattern("HH:mm"))

                        binding.timeText.visibility = View.VISIBLE
                        binding.refreshLayout.isRefreshing = false
                    }
                    else{
                        getQuote()
                    }
                }
            }
            else{
                binding.refreshLayout.isRefreshing = false

            }
        }

        var imageSlider = binding.sliderLayout

        var sliderImages = HashMap<String, Int>()
        sliderImages.put("The Parthenon", R.drawable.bg_parthenon)
        sliderImages.put("The Great Wave", R.drawable.bg_great_wave)
        sliderImages.put("The Starry Night", R.drawable.bg_the_starry_night)
        sliderImages.put("The School of Athens", R.drawable.bg_school_of_athens)
        sliderImages.put("Nighthawks", R.drawable.bg_nighthawks)
        sliderImages.put("Insomniac", R.drawable.bg_insomniac)

        for (name in sliderImages.keys) {
            val textSliderView = TextSliderView(binding.root.context)
            // initialize a SliderLayout
            sliderImages.get(name)?.let {
                textSliderView
                    .description(name)
                    .image(it)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
            }

            //add your extra information
            textSliderView.bundle(Bundle())
            textSliderView.bundle
                .putString("extra", name)
            imageSlider.addSlider(textSliderView)
        }
        imageSlider.setPresetTransformer(SliderLayout.Transformer.DepthPage)
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
        imageSlider.setCustomAnimation(DescriptionAnimation())
        imageSlider.setDuration(10000)

//        binding.todoTab.setOnClickListener {
//            findNavController().navigate(R.id.action_nav_home_to_nav_todo)
//        }

        return root
    }

    private fun getQuote(){
        AndroidNetworking.get("https://api.themotivate365.com/stoic-quote")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(response: JSONObject) {
                    val author: String = response.get("author").toString()
                    val quote: String = response.get("quote").toString()
                    quote_1 = Quote(author, quote, LocalDateTime.now().toString())

                    var last_upd = LocalDateTime.now()
                    quote_1.last_updated = last_upd.toString()

                    if(_binding != null){
                        binding.progressBar.stop()
                        binding.progressBar.visibility = View.GONE
                        binding.refreshLayout.isRefreshing = false
                        binding.timeText.visibility = View.VISIBLE
                        quote_1_textView.text = "\"" + quote_1.quote + "\" - " + quote_1.author
                        binding.timeText.text = last_upd.format(DateTimeFormatter.ofPattern("HH:mm"))

                        binding.timeText.visibility = View.VISIBLE

                        val firebaseUser = authManager.firebaseUser
                        database.child(path).child(firebaseUser!!.uid).removeValue()
                        database.child(path).child(firebaseUser!!.uid).push().setValue(quote_1).addOnSuccessListener { e->
                        }.addOnFailureListener{ e->
                            Toast.makeText(binding.root.context, "Error saving quote: $e", Toast.LENGTH_LONG).show()
                        }
                    }

                }
                override fun onError(anError: ANError?) {
                    SuperActivityToast.create(requireActivity(), Style(), Style.TYPE_STANDARD)
                        .setText("Error with api: ${anError?.message}")
                        .setDuration(Style.DURATION_LONG)
                        .setFrame(Style.FRAME_LOLLIPOP)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                        .setAnimations(Style.ANIMATIONS_POP).show()
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings ->{
            findNavController().navigate(R.id.action_nav_home_to_settingsFragment)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}