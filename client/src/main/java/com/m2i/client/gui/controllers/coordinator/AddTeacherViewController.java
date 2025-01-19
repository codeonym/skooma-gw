// AddTeacherController.java
package com.m2i.client.gui.controllers.coordinator;

import com.m2i.client.utils.ServiceLocator;
import com.m2i.client.utils.SessionManager;
import com.m2i.shared.dto.TeacherRequestDTO;
import java.rmi.RemoteException;
import java.util.regex.Pattern;

import com.m2i.shared.interfaces.CoordinatorService;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AddTeacherViewController extends AddUserCommon {
    private static final String TEACHER_VIEW = "teacher";

    static {
        VALIDATION_PATTERNS.put("teacherId", Pattern.compile("^[A-Z]{2}[0-9]{6}$"));
        VALIDATION_PATTERNS.put("officeNumber", Pattern.compile("^[1-9]+$"));
    }

    @FXML private MFXTextField department;
    @FXML private MFXTextField officeNumber;
    @FXML private MFXTextField teacherIdentifier;
    @FXML private Label departmentError;
    @FXML private Label officeNumberError;
    @FXML private Label teacherIdentifierError;

    private final CoordinatorService coordService;

    public AddTeacherViewController() {
        this.coordService = ServiceLocator.getInstance().getCoordinatorService();
    }

    @Override
    protected void initializeSpecificValidationConfigs() {
        addValidationConfig(department, departmentError, "Department is required",
                text -> text != null && !text.trim().isEmpty());
        addValidationConfig(officeNumber, officeNumberError, "Office number must be numeric",
                text -> text != null && VALIDATION_PATTERNS.get("officeNumber").matcher(text).matches());
        addValidationConfig(teacherIdentifier, teacherIdentifierError, "Teacher ID must be in format: XX123456",
                text -> text != null && VALIDATION_PATTERNS.get("teacherId").matcher(text.toUpperCase()).matches());
    }

    @Override
    protected void updateSpecificErrorBlocks() {
        hideBlock(block1, firstNameError, lastNameError);
        hideBlock(block2, emailError, cniError);
        hideBlock(block3, departmentError, officeNumberError, teacherIdentifierError);
    }

    @Override
    protected void register() throws RemoteException {
        TeacherRequestDTO dto = new TeacherRequestDTO();
        dto.setFirstName(firstName.getText().trim());
        dto.setLastName(lastName.getText().trim());
        dto.setEmail(email.getText().trim());
        dto.setDepartment(department.getText().trim());
        dto.setOfficeNumber(officeNumber.getText().trim());
        dto.setTeacherIdentifier(teacherIdentifier.getText().trim().toUpperCase());

        coordService.registerTeacher(
                SessionManager.getInstance().getCurrentSession().getSessionId(),
                dto
        );
    }
}