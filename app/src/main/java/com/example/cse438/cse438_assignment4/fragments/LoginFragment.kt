package com.example.cse438.cse438_assignment4.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cse438.cse438_assignment4.PlayBoardActivity

import com.example.cse438.cse438_assignment4.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LoginFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_login.setOnClickListener(){
            if (login_email.text!=null &&login_email.text.toString()!="" && login_password.text!=null && login_password.text.toString()!=""){
                val email = login_email.text.toString()
                val password = login_password.text.toString()
                button_login.isEnabled = false
                signIn(email,password)
            }else{
                Toast.makeText(context, "Please Input valid email or password.",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun signIn(email:String,password:String){
        Toast.makeText(context, "Please Wait",
            Toast.LENGTH_SHORT).show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    button_login.isEnabled = true
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.",
                        Toast.LENGTH_LONG).show()
                    button_login.isEnabled = true
                }
            }
    }


    fun updateUI(user: FirebaseUser?){
        val intent = Intent(context,PlayBoardActivity::class.java)
        val bundle = Bundle()
        bundle.putString("username", user?.email)
        intent.putExtras(bundle)
        activity?.startActivity(intent)
    }

}
