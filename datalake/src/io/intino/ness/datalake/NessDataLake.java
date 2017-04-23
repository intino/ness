package io.intino.ness.datalake;

import java.util.List;

public interface NessDataLake {
    List<Tank> tanks();
    Tank get(String tank);

}
