package org.n52.shared.requests;

import java.io.Serializable;
import org.n52.shared.serializable.pojos.QCOptions;

public class QCDataRequest implements Serializable  {

    private static final long serialVersionUID = -5853951638828258828L;
    protected QCOptions options;

    
    private QCDataRequest() {
    }

    public QCDataRequest(QCOptions options) {
        this.options = options;
    }
    
    public QCOptions getOptions() {
        return this.options;
    }

}