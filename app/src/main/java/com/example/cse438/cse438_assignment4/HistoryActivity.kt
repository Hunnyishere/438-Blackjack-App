package com.example.cse438.cse438_assignment4

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cse438.cse438_assignment4.data.GameHistory
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore
    private  lateinit var  username: String
    private var historyList: ObservableList<GameHistory> = ObservableArrayList()
    private lateinit var lastAdapter: LastAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        //
        val intent = intent
        val bundle = intent.extras
        username = bundle!!.getString("username")!!
        histroy_title.text = username +"\n" + " Game History"
        // create an instance of the firebase database
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.firestoreSettings = settings
        //Queryhistory by email
        QueryHistory()
        val recyclerView:RecyclerView = recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        //create last adapter
        lastAdapter=LastAdapter(historyList, BR.item)
            .map<GameHistory>(R.layout.item_history)
            .into(recyclerView)

        return_button.setOnClickListener() {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

    }

    fun QueryHistory(){
        Log.v("email:",username)
        historyList.clear()
        historyList.add(GameHistory("Bets","","Your Points","Computer Points","Winner"))
        db.collection("gamehistory")
            .whereEqualTo("email", username)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful){
                    for (document in task.result!!) {
                        Log.v("find the data", "${document.id} => ${document.data}")
                        val history = GameHistory(document.get("bet").toString(),document.get("email").toString(),document.get("playercount").toString(),document.get("computercount").toString(),document.get("winner").toString())
                        historyList.add(history)
                    }
                }else
                    Log.v("queryhistory","null")

            })

   }

}
