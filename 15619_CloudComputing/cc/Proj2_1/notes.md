Notes

---

# Work Flow

    inst_load_generator <- PROC( AWS.EC2.StartInstance, m3.medium )
    inst_data_center <- PROC( AWS.EC2.StartInstance, m3.medium )
    PROC( Add, list< AWS::EC2::Instance >, inst_data_center ) 
    
    Start the horizontal scaling test.
    testId <- PROC( StartTest, inst_load_generator, inst_data_center )
    
    # Meet the RPS requirement in 30 minutes.
    rps_satisfied <- FALSE
    WHILE NOT rps_satisfied
        curr_rps <- PROC_CALL( GetCurrentRPS )
        IF curr_rps < REQUIRED_RPS THEN
            inst_new_data_center <- PROC( AWS.EC2.StartInstance, m3.medium )
            PROC( Add, list< AWS::EC2::Instance >, inst_new_data_center )
            PROC( NotifyNewInstance, inst_load_generator, new_instance )

---

# References

http://stackoverflow.com/questions/645312/what-is-the-quickest-way-to-http-get-in-python
https://docs.python.org/2/library/urllib2.html
https://docs.python.org/2/library/configparser.html
http://stackoverflow.com/questions/1059559/python-strings-split-with-multiple-delimiters
http://stackoverflow.com/questions/510348/how-can-i-make-a-time-delay-in-python
http://stackoverflow.com/questions/9573244/most-elegant-way-to-check-if-the-string-is-empty-in-python
http://stackoverflow.com/questions/415511/how-to-get-current-time-in-python

---

# Instance States

i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226  pending 0 subnet-131fcf38
i-d7b0f226 ec2-52-0-183-107.compute-1.amazonaws.com running 16 subnet-131fcf38

---

# LG: m3.medium
# DC: m1.small: $0.044

; 2015-02-04T00:56:05+0000
; Vertical Scaling Test
; Test launched. Please check every minute for update.
[Test]
type=vertical
testId=1423011365205
testFile=test.1423011365205.log

[Minute 1]
rps=259.79

[Minute 2]
rps=593.73

[Minute 3]
rps=585.40

[Minute 4]
rps=580.76

[Minute 5]
rps=575.96

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-15f199e4
instanceType=m3.medium
publicHostname=ec2-52-0-14-111.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Data Center]
instanceType=m1.small
andrewId=yaobinw
availabilityZone=us-east-1e
instanceId=i-02f9842d
amiId=ami-b04106d8
publicHostname=ec2-52-0-1-233.compute-1.amazonaws.com
awsId=172090027681

; MSB is validating...
; MSB congratulates you on completing vertical scaling test on m1.small!
; Your mean rps of last 4 minutes for this type is 583.96.
; Test finished

---

# LG: m3.medium
# DC: m1.medium: $0.087

; 2015-02-04T01:11:42+0000
; Vertical Scaling Test
; Test launched. Please check every minute for update.
[Test]
type=vertical
testId=1423012302686
testFile=test.1423012302686.log

[Minute 1]
rps=757.68

[Minute 2]
rps=1162.15

[Minute 3]
rps=1138.41

[Minute 4]
rps=1126.86

[Minute 5]
rps=1115.95

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-15f199e4
instanceType=m3.medium
publicHostname=ec2-52-0-14-111.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Data Center]
instanceType=m1.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-352f44c4
amiId=ami-b04106d8
publicHostname=ec2-52-0-38-14.compute-1.amazonaws.com
awsId=172090027681

; MSB is validating...
; MSB congratulates you on completing vertical scaling test on m1.medium!
; Your mean rps of last 4 minutes for this type is 1135.84.
; Test finished

---

# LG: m3.medium
# DC: m1.large: $0.175

; 2015-02-04T01:29:28+0000
; Vertical Scaling Test
; Test launched. Please check every minute for update.
[Test]
type=vertical
testId=1423013368940
testFile=test.1423013368940.log

[Minute 1]
rps=1987.90

[Minute 2]
rps=2453.07

[Minute 3]
rps=2389.24

[Minute 4]
rps=2328.44

[Minute 5]
rps=2430.18

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-15f199e4
instanceType=m3.medium
publicHostname=ec2-52-0-14-111.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Data Center]
instanceType=m1.large
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-a9264d58
amiId=ami-b04106d8
publicHostname=ec2-52-0-54-203.compute-1.amazonaws.com
awsId=172090027681

; MSB is validating...
; MSB congratulates you on completing vertical scaling test on m1.large!
; Your mean rps of last 4 minutes for this type is 2400.23.
; Test finished


---

# HTTP GET Response Content

<!DOCTYPE html><html><head><title>MSB Load Generator</title></head><body><a href='/log?name=test.1423081054674.log'>Test</a> launched.</body></html>

---

# Horizontal Testing Log: 1 instance

; 2015-02-08T00:48:51+0000
; Horizontal Scaling Test
; You used your browser to start this test. 
; The result won't be reported to MSB. 
; Test launched. Please check every minute for update.
; Your goal is too achieve rps=4000 in 30 min
; Minimal interval of adding instances is 100 sec
[Test]
type=horizontal
testId=1423356531889
testFile=test.1423356531889.log

[Minute 1]
ec2-52-0-175-106.compute-1.amazonaws.com=276.93

[Minute 2]
ec2-52-0-175-106.compute-1.amazonaws.com=626.22

[Minute 3]
ec2-52-0-175-106.compute-1.amazonaws.com=576.93

[Minute 4]
ec2-52-0-175-106.compute-1.amazonaws.com=604.93

[Minute 5]
ec2-52-0-175-106.compute-1.amazonaws.com=597.00
ec2-52-0-155-22.compute-1.amazonaws.com=240.83

[Minute 6]
ec2-52-0-175-106.compute-1.amazonaws.com=606.95
ec2-52-0-155-22.compute-1.amazonaws.com=593.13

[Minute 7]
ec2-52-0-175-106.compute-1.amazonaws.com=599.21
ec2-52-0-155-22.compute-1.amazonaws.com=590.22

[Minute 8]
ec2-52-0-175-106.compute-1.amazonaws.com=503.44
ec2-52-0-155-22.compute-1.amazonaws.com=584.63
ec2-52-0-183-74.compute-1.amazonaws.com=246.22


---

# My Final Log

; 2015-02-08T20:30:25+0000
; Horizontal Scaling Test
; Test launched. Please check every minute for update.
; Your goal is too achieve rps=4000 in 30 min
; Minimal interval of adding instances is 100 sec
[Test]
type=horizontal
testId=1423427425473
testFile=test.1423427425473.log

[Minute 1]
ec2-52-0-137-154.compute-1.amazonaws.com=596.44

[Minute 2]
ec2-52-0-137-154.compute-1.amazonaws.com=915.72

[Minute 3]
ec2-52-0-137-154.compute-1.amazonaws.com=912.12

[Minute 4]
ec2-52-0-137-154.compute-1.amazonaws.com=912.27

[Minute 5]
ec2-52-0-137-154.compute-1.amazonaws.com=898.46

[Minute 6]
ec2-52-0-137-154.compute-1.amazonaws.com=858.90
ec2-52-0-73-66.compute-1.amazonaws.com=623.50

[Minute 7]
ec2-52-0-137-154.compute-1.amazonaws.com=874.00
ec2-52-0-73-66.compute-1.amazonaws.com=950.48

[Minute 8]
ec2-52-0-137-154.compute-1.amazonaws.com=876.87
ec2-52-0-73-66.compute-1.amazonaws.com=962.18

[Minute 9]
ec2-52-0-137-154.compute-1.amazonaws.com=854.21
ec2-52-0-73-66.compute-1.amazonaws.com=921.76

[Minute 10]
ec2-52-0-137-154.compute-1.amazonaws.com=858.90
ec2-52-0-73-66.compute-1.amazonaws.com=952.99

[Minute 11]
ec2-52-0-137-154.compute-1.amazonaws.com=714.71
ec2-52-0-73-66.compute-1.amazonaws.com=913.18
ec2-52-0-212-33.compute-1.amazonaws.com=565.23

[Minute 12]
ec2-52-0-137-154.compute-1.amazonaws.com=711.17
ec2-52-0-73-66.compute-1.amazonaws.com=918.46
ec2-52-0-212-33.compute-1.amazonaws.com=879.17

[Minute 13]
ec2-52-0-137-154.compute-1.amazonaws.com=720.26
ec2-52-0-73-66.compute-1.amazonaws.com=913.85
ec2-52-0-212-33.compute-1.amazonaws.com=904.97

[Minute 14]
ec2-52-0-137-154.compute-1.amazonaws.com=717.22
ec2-52-0-73-66.compute-1.amazonaws.com=914.36
ec2-52-0-212-33.compute-1.amazonaws.com=900.54

[Minute 15]
ec2-52-0-137-154.compute-1.amazonaws.com=819.57
ec2-52-0-73-66.compute-1.amazonaws.com=760.39
ec2-52-0-212-33.compute-1.amazonaws.com=861.75
ec2-52-0-147-132.compute-1.amazonaws.com=604.52

[Minute 16]
ec2-52-0-137-154.compute-1.amazonaws.com=828.17
ec2-52-0-73-66.compute-1.amazonaws.com=759.35
ec2-52-0-212-33.compute-1.amazonaws.com=855.67
ec2-52-0-147-132.compute-1.amazonaws.com=939.57

[Minute 17]
ec2-52-0-137-154.compute-1.amazonaws.com=831.04
ec2-52-0-73-66.compute-1.amazonaws.com=757.60
ec2-52-0-212-33.compute-1.amazonaws.com=830.81
ec2-52-0-147-132.compute-1.amazonaws.com=941.27

[Minute 18]
ec2-52-0-137-154.compute-1.amazonaws.com=830.34
ec2-52-0-73-66.compute-1.amazonaws.com=756.20
ec2-52-0-212-33.compute-1.amazonaws.com=856.95
ec2-52-0-147-132.compute-1.amazonaws.com=944.28

[Minute 19]
ec2-52-0-137-154.compute-1.amazonaws.com=842.42
ec2-52-0-73-66.compute-1.amazonaws.com=757.60
ec2-52-0-212-33.compute-1.amazonaws.com=861.27
ec2-52-0-147-132.compute-1.amazonaws.com=942.00

[Minute 20]
ec2-52-0-137-154.compute-1.amazonaws.com=829.82
ec2-52-0-73-66.compute-1.amazonaws.com=880.77
ec2-52-0-212-33.compute-1.amazonaws.com=730.45
ec2-52-0-147-132.compute-1.amazonaws.com=926.02
ec2-52-0-125-134.compute-1.amazonaws.com=597.37

[Minute 21]
ec2-52-0-137-154.compute-1.amazonaws.com=818.38
ec2-52-0-73-66.compute-1.amazonaws.com=894.36
ec2-52-0-212-33.compute-1.amazonaws.com=701.52
ec2-52-0-147-132.compute-1.amazonaws.com=929.38
ec2-52-0-125-134.compute-1.amazonaws.com=927.66

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-b9e4ab48
instanceType=m3.medium
publicHostname=ec2-52-0-190-75.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Data Center 0]
instanceType=m3.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-7ae6a98b
amiId=ami-b04106d8
publicHostname=ec2-52-0-137-154.compute-1.amazonaws.com
awsId=172090027681

[Data Center 1]
instanceType=m3.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-eee1ae1f
amiId=ami-b04106d8
publicHostname=ec2-52-0-73-66.compute-1.amazonaws.com
awsId=172090027681

[Data Center 2]
instanceType=m3.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-07feb1f6
amiId=ami-b04106d8
publicHostname=ec2-52-0-212-33.compute-1.amazonaws.com
awsId=172090027681

[Data Center 3]
instanceType=m3.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-1dfbb4ec
amiId=ami-b04106d8
publicHostname=ec2-52-0-147-132.compute-1.amazonaws.com
awsId=172090027681

[Data Center 4]
instanceType=m3.medium
andrewId=yaobinw
availabilityZone=us-east-1b
instanceId=i-55f7b8a4
amiId=ami-b04106d8
publicHostname=ec2-52-0-125-134.compute-1.amazonaws.com
awsId=172090027681

; MSB is validating...
; MSB congratulates you on completing horizontal scaling test!
; You have achieved your goal in 21 minutes.
; Have you noticed that the load varies across different instances?
; Test finished

---

# ELB Test #1

; 2015-02-08T21:00:59+0000
; Elastic Load Balancer Test
; Test launched. Please check every minute for update.
[Test]
type=elb
testId=1423429259017
testFile=test.1423429259017.log

[Minute 1]
rps=428.56

[Minute 2]
rps=437.40

[Minute 3]
rps=17.19

[Minute 4]
rps=425.18

[Minute 5]
rps=17.55

[Minute 6]
rps=447.58

[Minute 7]
rps=432.80

[Minute 8]
rps=15.30

[Minute 9]
rps=463.54

[Minute 10]
rps=428.17

[Minute 11]
rps=10.42

[Minute 12]
rps=461.25

[Minute 13]
rps=438.70

[Minute 14]
rps=618.43

[Minute 15]
rps=49.65

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-b9e4ab48
instanceType=m3.medium
publicHostname=ec2-52-0-190-75.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Elastic Load Balancer]
dns=cc-proj-21-menlopark-1948829782.us-east-1.elb.amazonaws.com

; MSB is validating...
; Your rps = 304.511414 seems too low. Please try again if you have budget left.
; Please be sure that ELB is connected to all instances you launched in Manual Scaling Test.
; Please make sure all instances connected are healthy.
; Test finished


---

#ELB Test #2

; 2015-02-08T21:19:34+0000
; Elastic Load Balancer Test
; Test launched. Please check every minute for update.
[Test]
type=elb
testId=1423430374685
testFile=test.1423430374685.log

[Minute 1]
rps=432.26

[Minute 2]
rps=29.10

[Minute 3]
rps=461.92

[Minute 4]
rps=436.63

[Minute 5]
rps=474.62

[Minute 6]
rps=34.12

[Minute 7]
rps=433.45

[Minute 8]
rps=49.83

[Minute 9]
rps=465.37

[Minute 10]
rps=1400.52

[Minute 11]
rps=90.58

[Minute 12]
rps=1385.68

[Minute 13]
rps=1407.19

[Minute 14]
rps=90.93

[Minute 15]
rps=1432.00

[Load Generator]
awsId=172090027681
andrewId=yaobinw
amiId=ami-4c4e0f24
instanceId=i-b9e4ab48
instanceType=m3.medium
publicHostname=ec2-52-0-190-75.compute-1.amazonaws.com
availabilityZone=us-east-1b

[Elastic Load Balancer]
dns=cc-proj-21-menlopark-1948829782.us-east-1.elb.amazonaws.com

; MSB is validating...
; MSB congratulates you on completing elastic load balancer test!
; Have you seen there is a slow increase in rps and the load is balanced across all instances?
; Why is the rps of Elastic Load Balancer Test often smaller than that of Horizontal Scaling Test?
; Test finished
