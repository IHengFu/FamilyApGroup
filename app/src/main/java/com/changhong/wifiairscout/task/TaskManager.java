package com.changhong.wifiairscout.task;

import java.util.Observable;
import java.util.Observer;


public class TaskManager  extends Observable {
//    private static final String TAG = "TaskManager";
    private static TaskManager sInstance;
	private TaskManager(){}
    public static final Integer CANCEL_ALL = 1;
    
    public static TaskManager getInstance(){
    	if(sInstance==null){
    		sInstance=new TaskManager();
    	}
    	return sInstance;
    }
    
    public void cancelAll() {
        setChanged();
        notifyObservers(CANCEL_ALL);
    }
    
    
    public void addTask(Observer task) {
        super.addObserver(task);
    }
}