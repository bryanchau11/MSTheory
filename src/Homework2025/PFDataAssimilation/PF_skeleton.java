/*  Georgia State University
 *  CSC 8840 - Modeling and Simulation Theory and Application
 *  This java class is part of a class project that involves using particle filter 
 *  to carry out data assimilation for a DEVS-based discrete event simulation model
 *
 *  Author    : Prof. Xiaolin Hu
 *  Date      : 10-14-2024
 */

package Homework2025.PFDataAssimilation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import genDevs.modeling.DevsInterface;
import genDevs.simulation.*;

public class PF_skeleton {
	public double[] weight = new double[configData.numberofparticles]; // save particle's weights -- not normalized
	public double[] normalizedWeight = new double[configData.numberofparticles];
	int[] selectedParticles = new int[configData.numberofparticles];
	stateEntity[] particleStateArray = new stateEntity[configData.numberofparticles];
	stateEntity[] newParticleStateArray = new stateEntity[configData.numberofparticles];

	carWashSys_DA[] particleArray = new carWashSys_DA[configData.numberofparticles];

	// array to store data from the "real" system
	sensorDataEntity[] sensorDataArray = new sensorDataEntity[configData.numberofsteps];
	stateEntity[] realStateArray = new stateEntity[configData.numberofsteps];

	Random globalRand;

	PrintWriter DA_result, step_result;

	boolean batchRun = false;

	public PF_skeleton() {

		try {
			DA_result = new PrintWriter(new FileOutputStream(configData.DataPathName + "DAResult.txt"), true);
		} catch (FileNotFoundException e) {
			System.out.println("File creation error!!!!!");
		}

		DA_result.println("step" + "\t" + "time"
				+ "\t" + "real_workingState"
				+ "\t" + "real_elapseTime"
				+ "\t" + "real_carQueue"
				+ "\t" + "DA_workingState"
				+ "\t" + "DA_workingElapsedTime"
				+ "\t" + "DA_breakState"
				+ "\t" + "DA_breakElapsedTime"
				+ "\t" + "DA_combinedElapseTime"
				+ "\t" + "DA_carQueue"
				+ "\t" + "DA_carQueue_std");
	}

	void startComputing() {
		globalRand = new Random(configData.PF_globalRandSeed);

		// read observation data and store it in the sensorDataArray
		// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
		readRealSensorData();
		// run the PF algorithm
		// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
		for (int step = 0; step < configData.numberofsteps; step++) {
			System.out.println("Processing step: " + step);

			// Sampling and compute weights for all particles
			samplingAndComputeWeights(step);
			// Copy new particle states to current particle states for next iteration
			for (int i = 0; i < configData.numberofparticles; i++) {
				particleStateArray[i] = newParticleStateArray[i];
			}
			// Normalize weights
			normalizeWeights(configData.numberofparticles);

			// Resampling based on weights
			resampling(configData.numberofparticles, selectedParticles);

			// Analyze and save results
			analyzeAndSaveResults(0, step);
		}

		DA_result.close();
		System.out.println("!!!!!!!!!!!!!finishing all steps of PF algorithm");

	}

	public void samplingAndComputeWeights(int step) {

		for (int idx = 0; idx < configData.numberofparticles; idx++) { // for each particle

			carWashSys_DA simModel;

			// --------------- create particle -----------------------------
			String workingState;
			double phaseSigma;
			double elaspedTimeInPhase;
			int carQueueSize;

			if (step == 0) {

				// generate an initial random state
				// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
				workingState = (globalRand.nextDouble() < 0.5) ? "working" : "break";
				phaseSigma = getTruncatedNormalDistribution(0, configData.stepInterval * 2,
						configData.stepInterval,
						configData.stepInterval * 0.3);
				elaspedTimeInPhase = getTruncatedNormalDistribution(0, phaseSigma,
						phaseSigma * 0.5,
						phaseSigma * 0.2);
				carQueueSize = (int) getTruncatedNormalDistribution(0, 20, 5, 3);
			} else {
				// initialize the state based on the resampled particles from the previous step
				workingState = particleStateArray[selectedParticles[idx]].workingState;
				phaseSigma = particleStateArray[selectedParticles[idx]].sigma;
				elaspedTimeInPhase = particleStateArray[selectedParticles[idx]].elapseTimeInPhase;
				carQueueSize = particleStateArray[selectedParticles[idx]].carQueueSize;

				// Add noise to the state --- we need this Particle Rejuvenation step because
				// DEVS model is a deterministic model
				// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
				double noise_sigma = configData.stepInterval * 0.1;

				// Add Gaussian noise to phaseSigma
				// x_new = x + ε where ε ~ TN(0, σ²; low-x, high-x)
				// phaseSigma_new = phaseSigma_current + ε where ε ~ TN(0, noise_sigma²;
				// -phaseSigma_current, 60-phaseSigma_current)
				phaseSigma = getTruncatedNormalDistribution(0, configData.stepInterval * 2,
						phaseSigma,
						noise_sigma);

				// Add Gaussian noise to elapsed time
				elaspedTimeInPhase = getTruncatedNormalDistribution(0, phaseSigma,
						elaspedTimeInPhase,
						noise_sigma);

				// Add integer noise to car queue size
				int queueNoise = (int) Math.round(globalRand.nextGaussian() * 1.0);
				carQueueSize = Math.max(0, carQueueSize + queueNoise);
			}

			// create the simulation model and assign it to simModel
			// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
			simModel = new carWashSys_DA("particle_" + idx, carQueueSize, workingState, phaseSigma, elaspedTimeInPhase,
					globalRand); // store the model in the particleArray
			particleArray[idx] = simModel;
			//
			// --------------- run simulation -----------------------------
			coordinator temp_coord = new coordinator(simModel);
			temp_coord.initialize();
			temp_coord.simulate_TN(configData.stepInterval);
			// System.gc();

			// --------------- collect simulation results and store in the
			// newParticleStateArray -----------------------------
			if (simModel.trand.stateEnt == null)
				System.out.println("step=" + step + " idx=" + idx + " State Data Not Received!!!");

			stateEntity sEnt = simModel.trand.stateEnt.copy();
			newParticleStateArray[idx] = sEnt;

			// --------------- compute weight -----------------------------
			double wt = computeWeight(step, simModel.trand.numOfFinishedCars);
			weight[idx] = wt;
			// System.out.println("particle "+idx+" weight="+wt);
		}

	}

	public void analyzeAndSaveResults(int run, int step) {

		// summarize estimation results
		int workingStateCount = 0, breakStateCount = 0;
		double workingElapseTime_sum = 0, breakElapseTime_sum = 0, combinedElapseTime_sum = 0;
		try {
			step_result = new PrintWriter(
					new FileOutputStream(configData.DataPathName + "run" + run + "_step_" + step + ".txt"), true);
		} catch (FileNotFoundException e) {
			System.out.println("File creation error!!!!!");
		}
		step_result.println("particleIdx" + "\t" + "state" + "\t" + "sigma" + "\t" + "elapseTimeInPhase"
				+ "\t" + "carQueueSize" + "\t" + "arrivingCarCount" + "\t" + "departureCarCount");

		double carQueueSizeSum = 0;
		double[] carQArray = new double[configData.numberofparticles];
		for (int i = 0; i < configData.numberofparticles; i++) {
			String pState = particleStateArray[selectedParticles[i]].workingState;
			double sigma = particleStateArray[selectedParticles[i]].sigma;
			double elapseTimeInPhase = particleStateArray[selectedParticles[i]].elapseTimeInPhase;
			int carQueueSize = particleStateArray[selectedParticles[i]].carQueueSize;

			carQueueSizeSum += carQueueSize;
			carQArray[i] = carQueueSize;

			if (pState.startsWith("working")) {
				workingStateCount++;
				workingElapseTime_sum += elapseTimeInPhase;
				combinedElapseTime_sum += elapseTimeInPhase;
			} else if (pState.startsWith("break")) {
				breakStateCount++;
				breakElapseTime_sum += elapseTimeInPhase;
				combinedElapseTime_sum += elapseTimeInPhase;
			}

			int arrivingCarCount = particleArray[selectedParticles[i]].trand.numOfarrivingcars;
			int departureCarCount = particleArray[selectedParticles[i]].trand.numOfFinishedCars;

			step_result.println(i + "\t" + pState + "\t" + sigma + "\t" + elapseTimeInPhase + "\t" + carQueueSize + "\t"
					+ arrivingCarCount + "\t" + departureCarCount);
		}

		double aveQueue = carQueueSizeSum / configData.numberofparticles;
		double carQsize_std, carQsize_std_sum = 0;
		for (int i = 0; i < configData.numberofparticles; i++) {
			carQsize_std_sum += (carQArray[i] - aveQueue) * (carQArray[i] - aveQueue);
		}
		carQsize_std = Math.sqrt(carQsize_std_sum / configData.numberofparticles);

		DA_result.println(step + "\t" + step * configData.stepInterval
				+ "\t" + ((realStateArray[step].workingState.startsWith("working")) ? 20 : 0)
				+ "\t" + realStateArray[step].elapseTimeInPhase
				+ "\t" + realStateArray[step].carQueueSize
				+ "\t" + (workingStateCount * 1.0 / configData.numberofparticles * 20.0)
				+ "\t" + ((workingStateCount != 0) ? workingElapseTime_sum / workingStateCount : 0)
				+ "\t" + (breakStateCount * 1.0 / configData.numberofparticles * 20.0)
				+ "\t" + ((breakStateCount != 0) ? breakElapseTime_sum / breakStateCount : 0)
				+ "\t" + combinedElapseTime_sum / configData.numberofparticles
				+ "\t" + aveQueue
				+ "\t" + carQsize_std);
	}

	public double computeWeight(int step, int numFinishedCars) {
		// compute the weight
		double weight = 0;
		// ++++++++++++++++++++ Add your code below +++++++++++++++++++++++++
		int observedFinishedCars = sensorDataArray[step].finishedCarCount;

		// Compute difference between simulated and observed
		double difference = numFinishedCars - observedFinishedCars;

		// Compute weight using Gaussian likelihood function
		// Assuming observation noise with standard deviation sigma_obs
		double sigma_obs = 2.0; // observation noise standard deviation

		// Likelihood: exp(-0.5 * (difference/sigma)^2)
		weight = Math.exp(-difference * difference / (2 * sigma_obs * sigma_obs));

		return weight;
	}

	void normalizeWeights(int totalNumOfParticles) {
		double sumofWeights = 0;
		for (int i = 0; i < totalNumOfParticles; i++) {
			sumofWeights = sumofWeights + weight[i];
		}
		for (int j = 0; j < totalNumOfParticles; j++) {
			normalizedWeight[j] = weight[j] / sumofWeights;
		}
	}

	// the following method implements a standard resampling method
	public void resampling(int totalNumOfParticles, int[] selectedParticles) {
		double u_temperature[] = new double[totalNumOfParticles];
		double q_temperature[] = new double[totalNumOfParticles];

		for (int i = 0; i < totalNumOfParticles; i++) {
			if (i == 0) {
				q_temperature[i] = normalizedWeight[0];
			} else {
				q_temperature[i] = q_temperature[i - 1] + normalizedWeight[i];
			}
		}

		for (int j = 0; j < totalNumOfParticles; j++) {
			u_temperature[j] = globalRand.nextDouble();
		}
		java.util.Arrays.sort(u_temperature);

		int sampleIndex = 0;
		for (int j = 0; j < totalNumOfParticles; j++) {
			while (q_temperature[sampleIndex] < u_temperature[j]) {
				sampleIndex = sampleIndex + 1;
			}
			selectedParticles[j] = sampleIndex;
		}
	}

	public void readRealSensorData() {
		String realSensorDataFile = configData.DataPathName + configData.realSensorDataFileName;
		try {
			FileInputStream MyInputStream = new FileInputStream(realSensorDataFile);
			TextReader input;
			input = new TextReader(MyInputStream);

			// skip the first line
			input.readLine();

			for (int i = 0; i < configData.numberofsteps; i++) {
				// System.out.println("read data for step "+i);
				input.readDouble(); // skip the time
				int arrvCarCount = input.readInt();
				int finishedCarCount = input.readInt();

				int carQueue = input.readInt();
				int workingState = input.readInt();
				double sgm = input.readDouble();
				double elapseTimeInWorkingOrBreak = input.readDouble();
				String aa = input.readLine(); // skip the rest of the line

				sensorDataArray[i] = new sensorDataEntity(i, arrvCarCount, finishedCarCount);
				realStateArray[i] = new stateEntity(carQueue, ((workingState == 20) ? "working" : "break"), sgm,
						elapseTimeInWorkingOrBreak);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}

	public static void main(String[] args) {
		PF_skeleton PFA = new PF_skeleton();
		PFA.startComputing();
	}

	/////////////////////////////////////////////////////////////////////////////////
	private double getTruncatedNormalDistribution(double low, double high, double mean, double sigma) {
		double u = mean + sigma * globalRand.nextGaussian();
		while (u < low || u > high) // resample
			u = mean + sigma * globalRand.nextGaussian();
		return u;
	}

	public static double percentile(double[] latencies, double percentile) {
		int index = (int) Math.ceil(percentile / 100.0 * latencies.length);
		return latencies[index - 1];
	}
}
