package com.example.cse438.cse438_assignment4

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.example.cse438.cse438_assignment4.util.CardRandomizer
import kotlinx.android.synthetic.main.activity_play_board.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import android.R.attr.x
import android.app.Activity
import android.content.DialogInterface
import androidx.core.app.ComponentActivity.ExtraData
import android.content.Intent
import android.content.res.Resources
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.animation.*
import androidx.appcompat.app.AlertDialog
import com.example.cse438.cse438_assignment4.data.GameHistory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception
import kotlin.concurrent.timer


class PlayBoardActivity : AppCompatActivity() {

    private lateinit var randomizer: CardRandomizer
    private lateinit var cardList: ArrayList<Int>
    private lateinit var mDetector: GestureDetectorCompat
    private var computerRoundCount = 0
    private var playerRoundCount = 0
    private var startFlag = 0
    private var username = ""
    private var totalChips = 0
    private var bet = 0
    private var odds = 1
    private var oddFlag = true
    private var aceNumber = 0
    private var firstAceOwner = ""
    private var playercards: ArrayList<String> = ArrayList()
    private var computercards: ArrayList<String> = ArrayList()
    private var playercounts: Int = 0
    private var computercounts: Int = 0
    private lateinit var winner: String
    private lateinit var cardStackView: View
    private var i: Long = 0
    private var j: Long = 0
    private var endFlag = true
    private var winNum: Int = 0
    private var loseNum: Int = 0
    private lateinit var translateAnimation1: TranslateAnimation
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_board)
        mDetector = GestureDetectorCompat(this, MyGestureListener())
        cardStackView = imageView_top
        //before click newgame, bet operations are inavaliable
        editText_bet.visibility = View.INVISIBLE
        button_bet.visibility = View.INVISIBLE

        // set an instance of firebase
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.setFirestoreSettings(settings)
        button_newgame.setOnClickListener() {
            button_newgame.visibility = View.INVISIBLE
            NewGamePrepare(0)
        }
        button_leaderboard.setOnClickListener() {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
        button_logout.setOnClickListener() {
            FirebaseAuth.getInstance().signOut()
            finish()
        }

    }

    override fun onBackPressed() {
        // no return when click the back key
    }

    override fun onStart() {
        super.onStart()
        val intent = intent
        val bundle = intent.extras
        username = bundle!!.getString("username")!!
        textView_name.text = username
        randomizer = CardRandomizer()
        //cardList = randomizer.getIDs(this) as ArrayList<Int>
        cardList = randomizer.getIDs(this)
        button_bet.setOnClickListener() {
            if (!endFlag && oddFlag) {
                if (editText_bet.text != null && editText_bet.text.toString() != "") {
                    bet = editText_bet.text.toString().toInt()
                    if (bet > totalChips)
                        Toast.makeText(
                            baseContext,
                            "You have no much bet!",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        totalChips -= bet
                        // update database
                        queryDatabaseUpdateChips("set")
                        StartNewGame()
                    }
                } else
                    Toast.makeText(
                        baseContext,
                        "You have to place bet first!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            if (endFlag)
                Toast.makeText(
                    baseContext,
                    "You have to start a new game!",
                    Toast.LENGTH_SHORT
                ).show()
            if (!oddFlag) {
                Toast.makeText(baseContext, "You have to make choice!", Toast.LENGTH_SHORT).show()
            }

        }//button_bet
        button_history.setOnClickListener() {
            val intent = Intent(this, HistoryActivity::class.java)
            val bundle = Bundle()
            bundle.putString("username", username)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        queryDatabaseUpdateChips("get")

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    // allow animation go out of parent layout
    fun View.setAllParentsClip(enabled: Boolean) {
        var parent = parent
        while (parent is ViewGroup) {
            parent.clipChildren = enabled
            parent.clipToPadding = enabled
            parent = parent.parent
        }
    }

    fun dpToPixel(activity: Activity, dps: Int): Int {
        val r = activity.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), r.displayMetrics).toInt()
    }


    fun moveTo(targetView: ImageView, moveX:Float, moveY:Float, i: Long, who: String, aceFlag: Boolean) {


        translateAnimation1 = TranslateAnimation(
            targetView.x - moveX,
            targetView.x,
            targetView.y - moveY,
            targetView.y
        )
        translateAnimation1.setDuration(500)
        translateAnimation1.setInterpolator(DecelerateInterpolator())
        // wait for previous animations to finish
        translateAnimation1.setStartOffset(i * 500)
        targetView.setAllParentsClip(false)
        translateAnimation1.setAnimationListener(object : Animation.AnimationListener {
            var parent: ViewGroup = targetView.getParent() as ViewGroup
            override fun onAnimationStart(animation: Animation) {
                Log.v("Start Animation", parent.toString())
            }

            override fun onAnimationEnd(animation: Animation) {
                Log.v("End Animation", parent.toString())
                //judge ace_animation
                if (aceFlag)
                    aceAnimation(targetView, who)
                // judge if player loses
            }

            override fun onAnimationRepeat(animation: Animation) {
                Log.v("Repeat Animation", parent.toString())
            }
        });

        targetView.startAnimation(translateAnimation1)
        if (i>0){
            targetView.alpha = 0.0f
            var mStartVideoHandler: Handler = Handler()
            mStartVideoHandler.postDelayed(Runnable {
                targetView.alpha = 1.0f
            },i*500-200)
        }
    }

    fun aceAnimation(targetView: ImageView, who: String) {

        val animation2 = AnimationUtils.loadAnimation(this, R.anim.ace_anim)
        animation2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (aceNumber == 1) {
                    var mStartVideoHandler: Handler = Handler()
                    mStartVideoHandler.postDelayed(Runnable {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@PlayBoardActivity)
                        builder.setTitle("Lucky!!!")
                        builder.setMessage("You get a double chance since ace showed " + "\n\n" + "You have to make a choice before you do any operation"+
                        "\n\n"+"Swipe Up to double bet"+ "\n"+"Swipe Down to not do")
                        builder.setPositiveButton(
                            "Confirm",
                            DialogInterface.OnClickListener { dialog, which -> })
                        builder.create().show()
                    }, 1000)

                }
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        targetView.startAnimation(animation2)
    }


    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private var swipedistance = 150
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            Log.v("doble_click", "yes")
            if (endFlag) {
                Toast.makeText(baseContext, "You have to start a new game!", Toast.LENGTH_LONG)
                    .show()
            }
            if (startFlag == 0 && !endFlag && oddFlag)
                Toast.makeText(
                    baseContext,
                    "You have to place bet first!",
                    Toast.LENGTH_LONG
                ).show()
            if (startFlag == 1 && oddFlag) {
                placeCard(i)
            }
            if (!oddFlag) {
                Toast.makeText(
                    baseContext,
                    "You have to make a choice first!",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // swipe right
            if (e2.x - e1.x > swipedistance) {
                if (startFlag == 1){
                    if (oddFlag)
                        onSwingFunction()
                    else
                        Toast.makeText(
                            baseContext,
                            "You have to make a choice first!",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                else{//startFlag==0
                    if(oddFlag)
                        Toast.makeText(
                            baseContext,
                            "You have to start a new game or place bet!",
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        Toast.makeText(
                            baseContext,
                            "You have to make a choice first!",
                            Toast.LENGTH_SHORT
                        ).show()
                }

                return true
                }

            //swipe up
            if (e1.y - e2.y > swipedistance) {
                if (!oddFlag && !endFlag) {
                    odds = 2
                    Toast.makeText(baseContext, "You choose to double bets", Toast.LENGTH_SHORT)
                        .show()
                    oddFlag = true
                }
            }
            //swipe down
            if (e2.y - e1.y > swipedistance) {
                if (!oddFlag && !endFlag) {
                    odds = 1
                    Toast.makeText(
                        baseContext,
                        "You choose to give up double chance",
                        Toast.LENGTH_SHORT
                    ).show()
                    oddFlag = true
                }
            }
            return true
        }
    }


    fun onSwingFunction() {
        //computer's turn
        while (computercounts < 17) {
            PlaceCardToComputer(i)
            i++
        }
        //endgame
        endFlag = true
        endGame()
    }

    fun placeCard(i: Long) {
        val rand: Random = Random()
        var aceFlag: Boolean = false
        val r: Int = rand.nextInt(cardList.size)
        val id: Int = cardList.get(r)
        cardList.remove(r)
        val name: String = resources.getResourceEntryName(id)
        playercards.add(name)
        //judge name, count aceNumber
        val index: Int = name.indexOf("_")
        val cardname: String = name.substring(index + 1)
        if (cardname.equals("ace")) {
            aceNumber++
            //only in the top three round, can have a chance to double
            if (aceNumber == 1 && computerRoundCount < 3) {
                oddFlag = false
            }
            aceFlag = true
        }

        //image animation
        val imageView = ImageView(this)
        Log.v("player name", name)
        imageView.setImageResource(
            getResources().getIdentifier(
                name,
                "drawable",
                baseContext.getPackageName()
            )
        )
        imageView.layoutParams =
            ViewGroup.LayoutParams(computer_layout.width / 6, computer_layout.height / 2)
        player_layout.addView(imageView)
        //animation
        var cardStackLocation = IntArray(2)
        imageView_top.getLocationInWindow(cardStackLocation)
        var targetViewLocation = IntArray(2)
        imageView.getLocationInWindow(targetViewLocation)
        val moveY: Float = (targetViewLocation[1] - cardStackLocation[1] + (playerRoundCount / 6) * player_layout.height / 2).toFloat()
        var moveX = ((playerRoundCount % 6) * player_layout.width / 6).toFloat()
        playerRoundCount++
        //playboardLayout.bringChildToFront(player_layout)
        moveTo(imageView, moveX, moveY, i,"player", aceFlag)  // animation
        //after player get cards, judge if the game is over
        playercounts = CountCards("player")
        Log.v("Player count", playercounts.toString())
        endGame()
    }

    fun PlaceCardToComputer(i: Long) {
        val rand: Random = Random()
        val r: Int = rand.nextInt(cardList.size)
        var aceFlag: Boolean = false
        val id: Int = cardList.get(r)
        cardList.remove(r)
        val name: String = resources.getResourceEntryName(id)
        computercards.add(name)
        val imageView = ImageView(this)
        Log.v("computer name", name)
        if (computerRoundCount == 0)
            imageView.setImageResource(R.drawable.back)
        else {
            imageView.setImageResource(
                getResources().getIdentifier(
                    name,
                    "drawable",
                    baseContext.getPackageName()
                )
            )
            //judge name, count aceNumber, only judge up card
            val index: Int = name.indexOf("_")
            val cardname: String = name.substring(index + 1)
            if (cardname.equals("ace")) {
                aceNumber++
                //only in first and second round, can have a chance to double
                if (aceNumber == 1 && computerRoundCount < 2) {
                    oddFlag = false
                }
                aceFlag = true
            }

        }

        imageView.layoutParams = ViewGroup.LayoutParams(
            computer_layout.width / 6,
            computer_layout.height / 2
        )  //width:171px, height:184px
        computer_layout.addView(imageView)

        // animation
        var cardStackLocation = IntArray(2)
        imageView_top.getLocationInWindow(cardStackLocation)
        var targetViewLocation = IntArray(2)
        imageView.getLocationInWindow(targetViewLocation)
        val moveY: Float = (targetViewLocation[1] - cardStackLocation[1] + (computerRoundCount / 6)*computer_layout.height / 2).toFloat()
        var moveX = ((computerRoundCount % 6)*computer_layout.width / 6).toFloat()
        computerRoundCount++
        //playboardLayout.bringChildToFront(computer_layout)
        moveTo(imageView, moveX, moveY, i, "computer", aceFlag)  // animation
        computercounts = CountCards("computer")
        Log.v("Computer count", computercounts.toString())
    }

    fun CountCards(player: String): Int {
        var total = 0
        var aceNumber = 0
        var cardList: ArrayList<String> = ArrayList()
        val regEx = "[^0-9]"
        val p: Pattern = Pattern.compile(regEx)
        if (player.equals("player"))
            cardList = playercards
        else
            cardList = computercards
        //first deal all card expect for ACE
        for (card in cardList) {
            val m: Matcher = p.matcher(card)

            if (!m.replaceAll("").trim().equals("")) {
                val number = m.replaceAll("").trim().toInt()
                total += number
            } else {
                val index: Int = card.indexOf("_")
                val name: String = card.substring(index + 1)
                if (!name.equals("ace"))
                    total += 10
                else
                    aceNumber++
            }
        }
        //deal ACE
        if (aceNumber > 0) {
            // if total >= 11, ace = 1
            if (total >= 11)
                total += aceNumber
            //else has three conditions
            else {
                //if acenumer=1 & total <11, ace = 11
                if (aceNumber == 1)
                    total += 11
                // if total total+11+aceNumber-1 >21, no ace can be 11
                else if (aceNumber > 1 && total + aceNumber + 10 > 21)
                    total += aceNumber
                else if (aceNumber > 1 && total + aceNumber + 10 <= 21)
                    total += aceNumber + 10
            }
        }
        return total
    }

    fun queryDatabaseUpdateChips(event: String) {
        // query for document of this user
        db.collection("player")
            .whereEqualTo("playerName", username)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->

                if (task.isSuccessful) {
                    Log.v("Search in database", "Sucess")
                    println("search success !!!!!!!!!!!!!!!!!!")
                    for (document in task.result!!) {
                        if (event == "get") {
                            totalChips = document.get("chips").toString().toInt()
                            winNum = document.get("win").toString().toInt()
                            loseNum = document.get("lose").toString().toInt()
                        } else
                            document.reference
                                .update(
                                    mapOf(
                                        "chips" to totalChips,
                                        "win" to winNum,
                                        "lose" to loseNum
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d(
                                        "Database Update",
                                        "DocumentSnapshot successfully updated!"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "Database Update",
                                        "Error updating document",
                                        e
                                    )
                                }
                        textView_chips.text = "chips: " + totalChips.toString()
                        textView_win.text = "win: " + winNum.toString()
                        textView_lose.text = "lose: " + loseNum.toString()
                    }
                } else {
                    Log.v("Search in database", "Fail")
                    println("failed to get user data")
                }
            })
    }

    fun AddHistory() {
        //store values for the database
        val historyMap: MutableMap<String, Any> = HashMap()
        historyMap["email"] = username
        historyMap["playercount"] = playercounts.toString()
        historyMap["computercount"] = computercounts.toString()
        historyMap["bet"] = bet.toString()
        historyMap["winner"] = winner
        // Add a new document with a generated ID
        db.collection("gamehistory")
            .add(historyMap)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.v("add history success", "true")
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.v("add history success", "false")
            })
    }

    fun endGame() {
        // judge if player loses
        if (playercounts > 21) {
            turnUp()
            Toast.makeText(
                baseContext,
                "Game over! Computer wins! Total: computer: " + computercounts + ", you: " + playercounts,
                Toast.LENGTH_LONG
            ).show()
            loseNum++
            queryDatabaseUpdateChips("set")
            winner = "computer"
            AddHistory()
            NewGamePrepare(2000)
            return
        }
        if (endFlag) {
            // judge if computer loses
            if (computercounts > 21) {
                turnUp()
                Toast.makeText(
                    baseContext,
                    "Game over! You win! Total: computer: " + computercounts + ", you: " + playercounts,
                    Toast.LENGTH_LONG
                ).show()
                totalChips += (1 + odds) * bet
                winNum++
                queryDatabaseUpdateChips("set")
                winner = "you"
                AddHistory()
                NewGamePrepare(2000)
                return
            }
            // compare player and computer counts
            if (playercounts > computercounts) {
                turnUp()
                Toast.makeText(
                    baseContext,
                    "Game over! You win! Total: computer: " + computercounts + ", you: " + playercounts,
                    Toast.LENGTH_LONG
                ).show()
                totalChips += (1 + odds) * bet
                winNum++
                queryDatabaseUpdateChips("set")
                winner = "you"
                AddHistory()
                NewGamePrepare(2000)
            } else if (playercounts < computercounts) {
                turnUp()
                Toast.makeText(
                    baseContext,
                    "Game over! Computer wins! Total: computer: " + computercounts + ", you: " + playercounts,
                    Toast.LENGTH_LONG
                ).show()
                loseNum++
                queryDatabaseUpdateChips("set")
                winner = "computer"
                AddHistory()
                NewGamePrepare(2000)
            } else {
                turnUp()
                Toast.makeText(
                    baseContext,
                    "Game over! Scores are tied! Total: computer: " + computercounts + ", you: " + playercounts,
                    Toast.LENGTH_LONG
                ).show()
                totalChips += bet
                queryDatabaseUpdateChips("set")
                winner = "break even"
                AddHistory()
                NewGamePrepare(2000)
            }
        }
    }

    // when game finishes, turn up the first card of computer
    fun turnUp() {
        var name = computercards[0]
        var cardView: ImageView = computer_layout.getChildAt(0) as ImageView
        cardView.setImageResource(
            getResources().getIdentifier(
                name,
                "drawable",
                baseContext.getPackageName()
            )
        )
    }

    fun NewGamePrepare(waitTime: Long) {
        playercards.clear()
        computercards.clear()
        odds = 1
        oddFlag = true
        aceNumber = 0
        computerRoundCount = 0
        playerRoundCount = 0
        startFlag = 0
        endFlag = false
        i = 0
        j = 0

        var mStartVideoHandler: Handler = Handler()
        mStartVideoHandler.postDelayed(Runnable {
            player_layout.removeAllViews()
            computer_layout.removeAllViews()

            placeCard(i)
            i++
            placeCard(i)
            i++
            PlaceCardToComputer(i)  //back
            i++
            PlaceCardToComputer(i)  //front
            i++
            if (!translateAnimation1.hasStarted() || translateAnimation1.hasEnded())
            {
                i = 0
                editText_bet.visibility = View.VISIBLE
                button_bet.visibility = View.VISIBLE
                Toast.makeText(
                    baseContext,
                    "You can bet the chips and start a new game!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, waitTime)

    }

    fun StartNewGame(){
        editText_bet.visibility = View.INVISIBLE
        button_bet.visibility = View.INVISIBLE
        startFlag = 1
        Toast.makeText(baseContext, "You can begin the game!",Toast.LENGTH_SHORT).show()
    }

}
