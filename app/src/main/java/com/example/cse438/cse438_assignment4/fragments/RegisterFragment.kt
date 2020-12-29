package com.example.cse438.cse438_assignment4.fragments

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.cse438.cse438_assignment4.data.UserInDatabse

import com.example.cse438.cse438_assignment4.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.HashMap


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RegisterFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // create an instance of the firebase database
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.firestoreSettings = settings

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_signup.setOnClickListener(){
            if (signup_email.text!=null&&signup_email.text.toString()!="" && signup_password.text!=null && signup_password.text.toString()!=""){
                val email = signup_email.text.toString()
                val password = signup_password.text.toString()
                button_signup.isEnabled = false
                createAccount(email,password)
            }else
                Toast.makeText(context, "Please Input email or password.",
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun createAccount(email:String, password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
                    builder?.setMessage("Sign Up Successfully")
                    builder?.setPositiveButton("Confirm",DialogInterface.OnClickListener{dialog,which->
                        button_signup.isEnabled = true
                    })
                    builder?.create()?.show()
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.",
                        Toast.LENGTH_LONG).show()
                    button_signup.isEnabled = true
                }
            }
    }

    fun updateUI(user:FirebaseUser?){
        //create a new user
        val userInDatabse = UserInDatabse(
            user!!.email!!,
            100,
            0,
            0
        )

        //store values for the database
        val userMap: MutableMap<String, Any> = HashMap()
        userMap["playerName"] = userInDatabse.email
        userMap["chips"] = userInDatabse.chips
        userMap["win"] = userInDatabse.win
        userMap["lose"] = userInDatabse.lose


        // Add a new document with a generated ID
        db.collection("player")
            .add(userMap)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Toast.makeText(context,  "UserInDatabse created in the database!",Toast.LENGTH_LONG).show()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(context, "Failed to create user in the database!", Toast.LENGTH_LONG)
            })
    }
}
