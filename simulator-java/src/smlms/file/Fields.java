package smlms.file;

import java.util.HashMap;
import java.util.Iterator;

public enum Fields {

	ID ("id"),
	X ("x"),								// in nm
	Y ("y"),								// in nm
	Z ("z"),								// in nm
	FRAME ("frame"),
	PHOTONS ("photons"),
	CHANNEL ("channel"),
	FRAMEON ("frameon"),					// number of frame ON
	TOTAL ("total"),
	BACKGROUND_MEAN ("background"),
	BACKGROUND_STDEV ("noise"),
	SIGNAL_MEAN ("intensity"),
	SIGNAL_STDEV ("stdev"),
	SIGNAL_PEAK ("peak"),
	SIGMAX ("sigmax"),
	SIGMAY ("sigmay"),
	SIGMAZ ("sigmaz"),
	UNCERTAINTY ("uncertainty"),
	CLOSEST_ID ("closestid"),
	CLOSEST_DIST ("closestdistance"),
	CLOSEST_COUNT ("closestcount"),	
	PSNR ("psnr"),
	SNR ("snr"),
	CNR ("cnr"),
	UNKNOWN ("?"),
	NOTUSED ("*")
	;
	
	private final String fieldDescription;

    private Fields(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
    
    public static Fields getFields(int i, HashMap<Fields, Integer> fields) {
		Iterator<Fields> iterator = fields.keySet().iterator();
		while (iterator.hasNext()) {
			Fields mentry = iterator.next();
			if (i == fields.get(mentry)) 
				return mentry;
		}
		return null;
    }
}
