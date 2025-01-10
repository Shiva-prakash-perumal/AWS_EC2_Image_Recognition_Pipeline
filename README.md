# AWS Image and Text Recognition Pipeline

## Programming Assignment 1 - CS643 852: Cloud Computing

### Author
- **Name:** Shiva Prakash Perumal
- **NJIT ID:** 31620225
- **UCID:** sp3244
- **Email:** sp3244@njit.edu

## Introduction
This repository provides instructions for setting up an AWS environment and launching a pipeline that runs concurrently on two EC2 instances to perform image and text recognition. The pipeline utilizes the following AWS services:
- **S3**
- **SQS**
- **Rekognition**

## Setting Up the AWS Environment

### Step 1: Launch Two EC2 Instances
1. Go to the AWS Management Console and click **Launch instance**.
2. Enter a name for the EC2 instance.
3. Under "AMI," select **Amazon Linux 2023 AMI** and choose the **t2.micro** instance type.
4. Select a key pair (e.g., `vockey`).
5. Under Network Settings, create a security group with the following settings:
   - Allow SSH traffic from your IP (select **My IP** instead of **Anywhere**).
6. Repeat the steps to create another EC2 instance. Name the instances `EC2_C` and `EC2_T`.

### Step 2: Add IAM Roles to EC2 Instances
1. Ensure the instances are running. If not, go to **Instance state** > **Start instance**.
2. For each instance, go to **Actions** > **Security** > **Modify IAM role**.
3. Assign the `LabInstanceProfile` role from the dropdown.

### Step 3: Configure SQS
1. Access the SQS service in the AWS Management Console.
2. Create a new queue and select **FIFO queue**.
3. Configure the queue:
   - Provide a unique name ending with `.fifo`.
   - Enable **Content-Based Deduplication** and **High Throughput**.
4. Click **Create queue**.

## Working with Java Programs

### Step 1: Build and Package Java Projects
- Develop two Java programs for:
  - **Car Recognition**
  - **Text Recognition**
- Build the projects and generate executable JAR files.

### Step 2: Upload JAR Files to EC2 Instances
1. Download the PEM file for SSH access.
2. Set permissions for the PEM file:
   ```bash
   chmod 400 labsuser.pem
   ```
3. Connect to the EC2 instance:
   ```bash
   ssh -i <filename>.pem ec2-user@<public-ip>
   ```
   Type `yes` when prompted.
4. Use the `scp` command to upload the JAR files to the respective EC2 instances:
   ```bash
   scp -i <filename>.pem your-jar-file.jar ec2-user@<public-ip>:/home/ec2-user/
   ```

### Step 3: Run JAR Files on EC2 Instances
1. SSH into the EC2 instance.
2. List files to verify the JAR file is uploaded:
   ```bash
   ls
   ```
3. Run the JAR file:
   ```bash
   java -jar your-jar-file.jar
   ```

## Outputs

### EC2_C Instance
- Detects images containing cars.
- Outputs detection results and sends messages via the SQS queue.
- Example:
  - Total messages: 7
  - Messages related to car photographs: 6
  - End-of-index message: -1

### EC2_T Instance
- Reads messages from the SQS queue.
- Prints detected car labels and text from images.
- Outputs results to `out.txt`.

---

### Contact
For further queries, contact **sp3244@njit.edu**.

