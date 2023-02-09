package com.bek.lvlapp.ui.home

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bek.lvlapp.R
import com.bek.lvlapp.WelcomeActivity
import com.bek.lvlapp.databinding.FragmentHomeBinding
import com.bek.lvlapp.models.Quote
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var email = ""

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

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        quote_1_textView = binding.quote1

        binding.progressBar.start()

        quote_1 = Quote()

        val quoteListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(quote_1.last_updated == null){
                    for (quoteSnapshot in dataSnapshot.children) {
                        var quote = quoteSnapshot.getValue<Quote>()!!
                        quote_1 = Quote(quote.author, quote.quote, quote.last_updated)
                    }
                    if(_binding != null) {
                        if(quote_1.last_updated != null){
                            var last_upd = LocalDateTime.parse(quote_1.last_updated)
                            if(last_upd.isAfter(LocalDateTime.now().minusHours(1))){
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
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.DARK_GREY))
                        .setIconResource(R.drawable.ic_icon_copy)
                        .setHeight(87)
                        .setAnimations(Style.ANIMATIONS_SCALE)

                    toast.show()

                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }

        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

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

                    if(_binding != null){
                        binding.progressBar.visibility = View.GONE
                        binding.timeText.visibility = View.VISIBLE
                        quote_1_textView.text = "\"" + quote_1.quote + "\" - " + quote_1.author
                        binding.timeText.text = last_upd.format(DateTimeFormatter.ofPattern("HH:mm"))

                        binding.timeText.visibility = View.VISIBLE

                        val firebaseUser = firebaseAuth.currentUser
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

    private fun checkUser() {
        //check if user logged in
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            email = firebaseUser.email.toString();
        }
        else{
            binding.root.context.startActivity(Intent(binding.root.context, WelcomeActivity::class.java))
            val activity = context as Activity?
            activity!!.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}