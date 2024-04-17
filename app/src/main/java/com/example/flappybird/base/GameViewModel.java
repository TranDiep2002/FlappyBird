package com.example.flappybird.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flappybird.User;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
   public MutableLiveData<User> userLogin = new MutableLiveData<>(null);
  public   MutableLiveData<ArrayList<User>> usersArrayList = new MutableLiveData<>();


  public MutableLiveData<String> dataTest= new MutableLiveData<>("");

}
