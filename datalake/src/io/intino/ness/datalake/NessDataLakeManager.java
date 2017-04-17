package io.intino.ness.datalake;

public interface NessDataLakeManager {

    void stop(String channel);
    void pause(String channel);
    void resume(String channel);

}
