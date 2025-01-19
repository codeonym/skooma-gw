// AddStudentController.java
package com.m2i.client.gui.controllers.coordinator;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.dto.StudentRequestDTO;
import java.rmi.RemoteException;
import java.util.regex.Pattern;

import com.m2i.shared.interfaces.CoordinatorService;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AddStudentViewController extends AddUserCommon {
    private static final String STUDENT_VIEW = "student";

    static {
        VALIDATION_PATTERNS.put("cne", Pattern.compile("^[A-Z][0-9]{9}$"));
        VALIDATION_PATTERNS.put("apogee", Pattern.compile("^[0-9]{8}$"));
    }

    @FXML private MFXTextField apogee;
    @FXML private MFXTextField cne;
    @FXML private Label apogeeError;
    @FXML private Label cneError;

    private final CoordinatorService coordService;

    public AddStudentViewController() {
        this.coordService = ServiceLocator.getInstance().getCoordinatorService();
    }

    @Override
    protected void initializeSpecificValidationConfigs() {
        addValidationConfig(cne, cneError, "CNE must be in format: L123456789",
                text -> text != null && VALIDATION_PATTERNS.get("cne").matcher(text.toUpperCase()).matches());
        addValidationConfig(apogee, apogeeError, "Apogee must be 8 digits",
                text -> text != null && VALIDATION_PATTERNS.get("apogee").matcher(text).matches());
    }

    @Override
    protected void updateSpecificErrorBlocks() {
        hideBlock(block1, firstNameError, lastNameError);
        hideBlock(block2, emailError, cneError);
        hideBlock(block3, cniError, apogeeError);
    }

    @Override
    protected void register() throws RemoteException {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName(firstName.getText().trim());
        dto.setLastName(lastName.getText().trim());
        dto.setEmail(email.getText().trim());
        dto.setCne(cne.getText().trim().toUpperCase());
        dto.setCni(cni.getText().trim().toUpperCase());
        dto.setApogee(apogee.getText().trim());

        coordService.registerStudent(
                SessionManager.getInstance().getCurrentSession().getSessionId(),
                dto
        );
    }
}