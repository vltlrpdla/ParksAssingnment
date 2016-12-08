package com.example.user.monstercookie;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* created by May Lee
 * this approach thread section. 3
 * passing time is difficult to use outside of class
 * we need to use passingtime variable in innerclass
 **/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String LOG_TAG = "MainActivity";
    final int MSG_START_OVER = 0;
    final int MSG_STOP_SIMULATE = 1;
    final int MSG_UPDATE_TIMER = 2;
    final int MSG_UPDATE_M = 3;
    final int MSG_UPDATE_COOKIEJAR = 4;
    final int MSG_INIT_DATA = 5;


    int passingTime = 0;
    int poolSize = 5; //스레드풀 최대사이즈
    ExecutorService taskList;
    public static final int TIME_TO_REFRESH = 120;
    public static final int MAX_MONSTER_FOOD = 100;
    Monster monster;
    CookieJar cookieJar;


    TextView tvM1EatedNum, tvM2EatedNum, tvPassingTime,tvGrannybakedNum;
    Button  bStartSimulate, bStopSimulate;
    ProgressBar progressCircle,progressClock,progressGranny,progressM1,progressM2;

    private volatile boolean running;



    Handler mHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MSG_START_OVER: //스레드시작
                    startSimulate();
                    break;
                case MSG_STOP_SIMULATE: //스레드종료
                    Log.d(LOG_TAG,"ssssstop");
                    progressCircle.setVisibility(View.INVISIBLE);
                    stopSimulate();
                    break;
                case MSG_UPDATE_TIMER: //스레드타이머
                    tvPassingTime.setText("Simulation clock: "+ passingTime +"/120 sec");
                    progressClock.setProgress(passingTime);
                    break;
                case MSG_UPDATE_M: //몬스터 UI업데이트
                    monster = (Monster)msg.obj;
                    if(monster.getThreadName().equals("monster1")) {
                        tvM1EatedNum.setText("Total cookie eaten so far : "+monster.getEatedCookie()+"/100");
                        progressM1.setProgress(monster.getEatedCookie());
                    }else{
                        tvM2EatedNum.setText("Total cookie eaten so far : "+monster.getEatedCookie()+"/100");
                        progressM2.setProgress(monster.getEatedCookie());
                    }
                    break;
                case MSG_UPDATE_COOKIEJAR: //쿠키항아리 UI업데이트
                    tvGrannybakedNum.setText("Total baked cookie : " + cookieJar.getTotalCookie());
                    progressGranny.setProgress(cookieJar.getTotalCookie());
                    break;
                case MSG_INIT_DATA: //초기값
                    tvM1EatedNum.setText("Total cookie eaten so far : 0/100");
                    tvM2EatedNum.setText("Total cookie eaten so far : 0/100");
                    tvPassingTime.setText("Simulation clock: 0/120 sec");
                    tvGrannybakedNum.setText("Total baked cookie : 0" + cookieJar.getTotalCookie());
                    initProgress(0);
                default:
                    break;
            }
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvM1EatedNum = (TextView)findViewById(R.id.tvM1EatedNum);
        tvM2EatedNum = (TextView)findViewById(R.id.tvM2EatedNum);
        tvPassingTime = (TextView)findViewById(R.id.tvPassingTime);
        tvGrannybakedNum = (TextView)findViewById(R.id.tvGrannybakedNum);

        bStartSimulate = (Button)findViewById(R.id.bStartSimulate);
        bStopSimulate = (Button)findViewById(R.id.bStopSimulate);
        progressCircle = (ProgressBar)findViewById(R.id.progressCircle);
        progressClock = (ProgressBar)findViewById(R.id.progressClock);
        progressGranny = (ProgressBar)findViewById(R.id.progressGranny);
        progressM1 = (ProgressBar)findViewById(R.id.progressM1);
        progressM2 =(ProgressBar)findViewById(R.id.progressM2);

        taskList = Executors.newFixedThreadPool(poolSize);

        progressClock.setMax(TIME_TO_REFRESH);
        progressM1.setMax(TIME_TO_REFRESH);
        progressM2.setMax(TIME_TO_REFRESH);
        progressGranny.setMax(300);
        initProgress(0);
        progressCircle.setVisibility(View.INVISIBLE);

        bStartSimulate.setOnClickListener(this);
        bStopSimulate.setOnClickListener(this);
    }

    public void onClick(View v){

        Log.d(LOG_TAG,"온클릭");
        switch(v.getId()){
            case R.id.bStartSimulate:
                Log.d(LOG_TAG,"케이스");
                mHandler.sendEmptyMessage(MSG_START_OVER);
                break;
            case R.id.bStopSimulate:
                mHandler.sendEmptyMessage(MSG_STOP_SIMULATE);
                break;
            default:
                break;
        }

    }

    class Monster implements Runnable{

        String threadName;
        CookieJar cookieJar;
        private int eatedCookie = 0;

        public Monster(String threadName,CookieJar cookieJar){
            this.threadName = threadName;
            this.cookieJar = cookieJar;
        }

        @Override
        public void run() {
            while (running){
                //하나도 꺼내지 않아도 되고 남아 있는 쿠키를 전부 꺼내도 상관 없다
                //if 조건절로 Jar 항아리의 공유데이터의 남은 쿠키의 개수를 체크하고 남은 개수가 있을 경우 실행 벗 남은 개수랑 얘가 꺼내는 랜덤 넘버를 비교할 필요가 있을 것 같다.
                if( cookieJar.getLeftCookie() > 0) {

                    int randomNumber = randomRange(0, cookieJar.getLeftCookie());
                    cookieJar.takeCookie(randomNumber);// 공유 데이터
                    eatedCookie += randomNumber;

                    Message msg = new Message();
                    msg.what = MSG_UPDATE_M;
                    msg.obj = this;
                    mHandler.sendMessage(msg);
                    mHandler.sendEmptyMessage(MSG_UPDATE_COOKIEJAR);

                    Log.d(LOG_TAG,"monsterName : " +this.getThreadName()
                            + " eatedCookie : " + randomNumber
                            + " totalEatedCookie : " + this.getEatedCookie());
                }else{
                    Log.d(LOG_TAG,"exhausted!!");
                }

                if ( eatedCookie >= 100){
                    mHandler.sendEmptyMessage(MSG_STOP_SIMULATE);// 모든 스레드를 종료 시키고 승자를 화면에 보여준다 runOnUiThread
                }

                sleepSec(5);
            }
        }

        public int getEatedCookie(){ return eatedCookie; }

        public String getThreadName() {
            return threadName;
        }
    }


    class GrandMother implements Runnable{

        String threadName;
        CookieJar cookieJar;

        public GrandMother(String threadName, CookieJar cookieJar){
            this.threadName = threadName;
            this.cookieJar = cookieJar;
        }

        @Override
        public void run(){
            while( running ){
                int randomRangeNumber = randomRange(1, 10);
                cookieJar.bakeCookie(randomRangeNumber);

                Log.d(LOG_TAG,"granny bake cookie : "
                         + randomRangeNumber + " totalcookie : " + cookieJar.getLeftCookie());
                mHandler.sendEmptyMessage(MSG_UPDATE_COOKIEJAR);

                sleepSec(5);
                // 같은 공유 데이터의 변수를 수정하는 것 0~ 10
            }
        }
    }

    class CookieJar {
        private int leftCookie = 0;
        private int totalCookie = 0;

        public synchronized void bakeCookie(int randomNumber){
            leftCookie += randomNumber;
            totalCookie += randomNumber;
        }

        public synchronized void takeCookie(int randomNumber){ leftCookie -= randomNumber; }
        //race condition의 해결책

        public int getLeftCookie() {
            return leftCookie;
        }

        public int getTotalCookie() {
            return totalCookie;
        }
    }


    //UI 업데이트
    class SimulateClock implements Runnable{


        @Override
        public void run(){

            while(running){
                passingTime += 1;
                mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);

                if (passingTime == TIME_TO_REFRESH){
                    mHandler.sendEmptyMessage(MSG_STOP_SIMULATE);
                }

                sleepSec(1);
            }
        }
    }

    public static int randomRange(int n1, int n2) {
        return (int) (Math.random() * (n2 - n1 + 1)) + n1;
    }

    public void setRunning(boolean running){
        this.running = running;
    }


    public void startSimulate(){
        setRunning(true);
        progressCircle.setVisibility(View.VISIBLE);
        passingTime = 0;
        mHandler.sendEmptyMessage(MSG_INIT_DATA);
        cookieJar = new CookieJar();

        Log.d(LOG_TAG, "Log isTerminated : " + taskList.isTerminated()+ " Log isShutdown : " + taskList.isShutdown());

        taskList.execute(new SimulateClock());
        taskList.execute(new Monster("monster1",cookieJar));
        taskList.execute(new Monster("monster2",cookieJar));
        taskList.execute(new GrandMother("granny", cookieJar));
        Log.d(LOG_TAG,"스타트 끝");
    }

    //남아있는 스레드를 종료 시켜 줘야 한다.

    public void stopSimulate(){
        setRunning(false);
        // 스레드를 종료시키는 것
    }



    public void sleepSec(int sec){

        try {
            Thread.sleep(sec * 1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    //중복되는 곳이 많아 함수로 시작
    public void initProgress(int num){
        progressClock.setProgress(num);
        progressGranny.setProgress(num);
        progressM1.setProgress(num);
        progressM2.setProgress(num);
    }

}
