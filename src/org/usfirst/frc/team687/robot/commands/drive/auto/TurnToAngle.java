package org.usfirst.frc.team687.robot.commands.drive.auto;

import org.usfirst.frc.team687.robot.Robot;
import org.usfirst.frc.team687.robot.constants.DriveConstants;
import org.usfirst.frc.team687.robot.utilities.NerdyMath;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *@author Ted Lin
 */

public class TurnToAngle extends Command {

    private double m_desiredAngle;
    private double m_startTime, m_timeout;
    private double m_error;
    private double m_prevTimestamp;
    private double m_prevError;
    private double m_dTerm;

    private int m_counter;
    private int m_tolerance;

    /**
     * @param angle
     * @param timeout
     * @param tolerance
     */
    public TurnToAngle(double angle, int tolerance, double timeout) {
	m_desiredAngle = angle;
	m_tolerance = tolerance;
	m_timeout = timeout;

	// subsystem dependencies
	requires(Robot.drive);
    }

    @Override
    protected void initialize() {
	SmartDashboard.putString("Current Drive Command", "TurnToAngle");
	m_startTime = Timer.getFPGATimestamp();
	m_counter = 0;
    }

    @Override
    protected void execute() {
	double robotAngle = (360 - Robot.drive.getRawYaw()) % 360;
	m_error = -m_desiredAngle - robotAngle;
	m_error = (m_error > 180) ? m_error - 360 : m_error;
	m_error = (m_error < -180) ? m_error + 360 : m_error;

	m_dTerm = (m_prevError - m_error) / (m_prevTimestamp - Timer.getFPGATimestamp());
	m_prevTimestamp = Timer.getFPGATimestamp();

	double power = DriveConstants.kRotP * m_error + DriveConstants.kRotD * m_dTerm;
	power = NerdyMath.threshold(power, DriveConstants.kRotMinPower, DriveConstants.kRotPMaxPower);
	// power = Math.min(DriveConstants.kRotPMaxPower,
	// Math.max(-DriveConstants.kRotPMaxPower, power));

	Robot.drive.setPower(-power, power);
	if (Math.abs(m_error) <= DriveConstants.kDriveRotationTolerance) {
	    m_counter += 1;
	} else {
	    m_counter = 0;
	}
	m_prevError = m_error;
    }

    @Override
    protected boolean isFinished() {
	return m_counter > m_tolerance || Timer.getFPGATimestamp() - m_startTime > m_timeout;
	// return false;
    }

    @Override
    protected void end() {
	Robot.drive.setPowerZero();
    }

    @Override
    protected void interrupted() {
	end();
    }

}