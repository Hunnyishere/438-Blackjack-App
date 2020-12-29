package com.example.cse438.cse438_assignment4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cse438.cse438_assignment4.data.Leaderboard
import com.example.cse438.cse438_assignment4.data.UserInDatabse
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_leaderboard.*


class LeaderboardActivity : AppCompatActivity() {
    private var userList: ObservableList<UserInDatabse> = ObservableArrayList()
    private var rankList: ObservableList<Leaderboard> = ObservableArrayList()
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // set an instance of firebase
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.setFirestoreSettings(settings)

        // get rank
        getAllUser()

        val recyclerView: RecyclerView = leaderboard_recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        LastAdapter(rankList, BR.item)
            .map<Leaderboard>(R.layout.item_leaderboard)
            .into(recyclerView)

        return_button.setOnClickListener() {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
    }


    fun getAllUser(){
        db.collection("player")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.v("Get all users from db","Sucess")
                    println("search success !!!!!!!!!!!!!!!!!!")

                    userList.clear()
                    for (document in task.result!!) {
                        userList.add(
                            UserInDatabse(
                                document.get("playerName").toString(),
                                document.get("chips").toString().toInt(),
                                document.get("win").toString().toInt(),
                                document.get("lose").toString().toInt()
                            )
                        )
                    }
                    if(userList.size>0)
                        getRank()
                } else {
                    Log.v("Get all users from db","Fail")
                    println("failed to get user data")
                }
            })
    }

    fun getRank(){
        rankList.clear()
        rankList.add(Leaderboard("Rank","User","Chips"))
        var list = userList.sortedByDescending { it.chips }
        val iterator = list.iterator()
        for ((rank, user) in iterator.withIndex()) {
            val leaderboard = Leaderboard(
                (rank+1).toString(),
                user.email,
                (user.chips).toString()
            )
            rankList.add(leaderboard)
        }
    }


}