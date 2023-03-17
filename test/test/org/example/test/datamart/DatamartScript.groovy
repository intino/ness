package org.example.test.datamart

import io.intino.test.datahubtest.TestTerminal
import io.intino.test.datahubtest.datamarts.master.MasterDatamart

MasterDatamart datamart = getProperty("Datamart")

println datamart.applicationList()




