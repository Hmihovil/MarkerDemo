package com.example.tbrams.markerdemo.components;


import android.content.Context;
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Morse {

    Vibrator mVibrator;
    List<String> mRawMorse =new ArrayList<>();


    public Morse(Context context) {
        // Get instance of Vibrator from current Context
        mVibrator  = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public String getMorseCode(String text) {
        mRawMorse=getMorse(text);
        String result="";
        for (String word: mRawMorse) {
            result+=word+"\n\n";
        }

        return result;
    }



    public void vibrate() {
        if (mRawMorse !=null) {
            vibrate(mRawMorse);
        }
    }


    private List<String> getMorse(String input){
        String letters = "abcdefghijklmnopqrstuvwxyz0123456789æøå";
        String morseCode [] = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
                "----.",".----","..---","...--","....-",".....","-....","--...","---..","----.",".-.-","---.",".--.-"};

        String[] words = input.split(" ");
        List<String> morseWords = new ArrayList<>();
        for (String word: words){
            String result="";
            for (int i = 0; i < word.length(); i ++){
                for(int j = 0; j < letters.length(); j++){
                    if(word.toLowerCase().charAt(i) == letters.charAt(j)){
                        result+=morseCode[j]+" ";
                        break;
                    }
                }
            }
            result=result.trim();
            result=result.replace(".", "\u2022");
            result=result.replace("-", "\u2014");
            result=result.replace(" ", "\n");

            morseWords.add(result);
        }

        return morseWords;
    }



    private void vibrate(List<String> morseWords)  {

        int dot = 200;      // Length of a Morse Code "dot" in milliseconds
        int dash = 500;     // Length of a Morse Code "dash" in milliseconds
        int short_gap = 222;    // Length of Gap Between dots/dashes
        int medium_gap = 555;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words

        long[] pattern =new long[]{};
        int pause=0;

        for (String mWord: morseWords) {
            String[] symbols = mWord.split("\n");

            for (String symbolString: symbols) {
                pattern = addElement(pattern, pause);

                for (int i=0; i<symbolString.length();i++) {
                    if (symbolString.charAt(i)=='\u2022') {
                        pattern = addElement(pattern, dot);
                    } else if (symbolString.charAt(i)=='\u2014') {
                        pattern = addElement(pattern, dash);
                    }

                    if (i+1<symbolString.length()) {
                        if (symbolString.charAt(i+1)=='\u2022'||symbolString.charAt(i+1)=='\u2014') {
                            pattern = addElement(pattern, short_gap);
                        }
                    }
                }

                pause=medium_gap;
            }
            pause=long_gap;
        }

        // Only perform this pattern one time (-1 means "do not repeat")
        mVibrator.vibrate(pattern, -1);
    }

    private long[] addElement(long[] a, long e) {
        a  = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }

}
