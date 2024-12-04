package Modelo;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.verification.DPFPVerification;

public class DigitalPersonaConfig {

    private DPFPCapture lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();

    public DPFPCapture getLector() {
        return lector;
    }

    public void setLector(DPFPCapture lector) {
        this.lector = lector;
    }

    public DPFPEnrollment getReclutador() {
        return Reclutador;
    }

    public void setReclutador(DPFPEnrollment Reclutador) {
        this.Reclutador = Reclutador;
    }

    public DPFPVerification getVerificador() {
        return verificador;
    }

    public void setVerificador(DPFPVerification verificador) {
        this.verificador = verificador;
    }

}
