package net.kunmc.lab.player;

import java.util.Random;

public class PlayerAttribute {
    private final static int RANDOM_AGE_MAX = 100;
    private final static int START_AGE = 1;

    // 各世代の最高年齢
    private final static int MAX_BABY_AGE = 5;
    private final static int MAX_KIDS_AGE = 19;
    private final static int MAX_YOUNG_AGE = 29;
    private final static int MAX_ADULT_AGE = 65;
    private final static int MAX_ELDERLY_AGE = 99;

    private int age;
    private boolean isAging;
    private int generation;

    public PlayerAttribute() {
        this(new Random().nextInt(RANDOM_AGE_MAX));
    }
    public PlayerAttribute(int age) {
        this.age = age;
        this.isAging = false;
        this.generation = START_AGE;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void addAge() {
        this.age++;
    }

    public boolean isAging() {
        return this.isAging;
    }

    public void isAging(boolean isAging){
        this.isAging = isAging;
    }

    public int getGeneration() {
        return this.generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public void addGeneration() {
        this.generation ++;
    }

    /**
     * 現在の年齢がどの世代に該当するか判定する
     * @return int 世代(0-4), 老人の最大歳を超えた場合は-1を返す
     */
    public int findGeneration() {
        if(MAX_BABY_AGE >= this.age) {
            return 0;
        }
        if(MAX_KIDS_AGE >= this.age) {
            return 1;
        }
        if(MAX_YOUNG_AGE >= this.age) {
            return 2;
        }
        if(MAX_ADULT_AGE >= this.age) {
            return 3;
        }
        if(MAX_ELDERLY_AGE >= this.age) {
            return 4;
        }

        return -1;
    }
}
