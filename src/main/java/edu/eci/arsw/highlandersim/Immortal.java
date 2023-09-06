package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;

    private boolean pause = false;
    private boolean stop = false;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;
    private final List<Immortal> deadImmortals;
    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    


    public Immortal(String name, List<Immortal> immortalsPopulation, List<Immortal> deadImmortals,int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.deadImmortals = deadImmortals;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {
        
        while (health > 0 && !stop) {
            synchronized (this) {
                while (pause) {                      
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();    
                    }
                }
            }
            Immortal im;
            synchronized(immortalsPopulation){

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (pause) {
                synchronized (im) {
                    try {
                        im.wait();
                        pause = false;
                    } catch (InterruptedException e) {
    
                        e.printStackTrace();
                    }
                }
            }

        }
        synchronized (immortalsPopulation) {
            immortalsPopulation.remove(this);
        }
        // Agrega al "inmortal" a la lista de "inmortales" muertos
        synchronized (deadImmortals) {
            deadImmortals.add(this);
        }
    }

    public void fight(Immortal i2) {
        synchronized(immortalsPopulation){
            synchronized(i2){
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");

            }
        }
    }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pause() {
        pause = true;
    }

    public synchronized void resumes() {
        pause = false;
        notifyAll();
    }

    public void stopThread(){
        stop = true;
    }


}
