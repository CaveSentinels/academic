# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.2(CC-Proj-2.2)
# @author: robin
# @email: yaobinw@andrew.cmu.edu
# @tests:
#   1424042680822(best RPS but long instance hours)


# =============================================================================
# @type: directive
# @brief: Imports.

import urllib2
import time
import datetime
import re
import ConfigParser
import io
import sys

import boto.ec2
import boto.ec2.elb
import boto.ec2.elb.loadbalancer
import boto.ec2.autoscale
import boto.ec2.autoscale.tag
from boto.ec2.autoscale import LaunchConfiguration
from boto.ec2.autoscale import AutoScalingGroup
from boto.ec2.autoscale import ScalingPolicy
import boto.ec2.cloudwatch
from boto.ec2.cloudwatch import MetricAlarm
import boto.exception


# =============================================================================
# @type: constant
# @brief: AWS EC2 constants

_AWS_EC2_REGION_NORTH_VIRGINIA = "us-east-1"
_AWS_EC2_INSTANCE_URL_HTTP_PREFIX = "http://"


# =============================================================================
# @type: constant
# @brief: CC-Proj-2.2-related constants

_CC_PROJ_2_2_LG_AMI = "ami-ae0a46c6"  # LG = Load Generator
_CC_PROJ_2_2_DC_AMI = "ami-7c0a4614"  # DC = Data Center
_CC_PROJ_2_2_LG_INSTANCE_TYPE = "m3.medium"
_CC_PROJ_2_2_DC_INSTANCE_TYPE = "m3.medium"
_CC_PROJ_2_2_LG_SECURITY_GROUP_NAME = "HTTP-80-accessible"
_CC_PROJ_2_2_LG_SECURITY_GROUPS = [
    _CC_PROJ_2_2_LG_SECURITY_GROUP_NAME
]

_CC_PROJ_2_2_TAG_NAME = "Project"
_CC_PROJ_2_2_TAG_VALUE = "2.2"

_CC_PROJ_2_2_SECURITY_GROUP_NAME = "sg_CC_Proj_2_2_Free4All"
_CC_PROJ_2_2_SECURITY_GROUP_DESCRIPTION = \
    "Security Group for CC-Proj-2.2: Free for all."
_CC_PROJ_2_2_SECURITY_GROUPS = [
    _CC_PROJ_2_2_SECURITY_GROUP_NAME
]

_CC_PROJ_2_2_SG_RULE_PROTOCOL = "-1"
_CC_PROJ_2_2_SG_RULE_PORT_FROM = 1
_CC_PROJ_2_2_SG_RULE_PORT_TO = 65535
_CC_PROJ_2_2_SG_RULE_CIDR_IP = "0.0.0.0/0"

_CC_PROJ_2_2_SCALE_POLICY_UP_NAME = "ScalePolicy_Up"
_CC_PROJ_2_2_SCALE_POLICY_DOWN_NAME = "ScalePolicy_Down"
_CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME = 'ASG_CC_Proj_2_2'
_CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_TYPE_CHANGE_IN_CAPACITY = 'ChangeInCapacity'
_CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_DOWN = -1
_CC_PROJ_2_2_SCALE_POLICY_COOLDOWN = 60
_CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_UP = 1

_CC_PROJ_2_2_RESOURCE_DELETION_RETRY_INTERVAL = 5
_CC_PROJ_2_2_RESOURCE_DELETION_RETRIES = 36

_CC_PROJ_2_2_LG_INSTANCE_STATE_QUERY_INTERVAL = 5

_CC_PROJ_2_2_WARM_UP_TRIES = 5
_CC_PROJ_2_2_WARM_UP_TESTING_WAIT_DURATION = 360 # 360 seconds = 6 minutes

_CC_PROJ_2_2_WARM_UP_URL_MIDDLE = "/warmup?dns="
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_START_TESTING_URL_MIDDLE = "/junior?dns="
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_LOG_URL_MIDDLE = "/log?name="
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_LOG_FILE_NAME_PATTERN = "test\.[\d]*\.log"
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_RPS_LOG_SECTION_NAME_PATTERN = "Minute\ \d+"
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_TEST_END = "Test End"
_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_UNKNOWN_TEST_PROGRESS = "Unknown progress. Test may not start yet."

_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_CHECK_AVAILABILITY_INTERVAL = 30

_CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_COMPLETION_CHECK_INTERVAL = 120

_CC_PROJ_2_2_AUTO_SCALE_INSTANCE_SHUT_DOWN_TIME = 120

_CC_PROJ_2_2_ELB_HEALTH_CHECK_INTERVAL = 30
_CC_PROJ_2_2_ELB_HEALTH_CHECK_HEALTHY_THRESHOLD = 5
_CC_PROJ_2_2_ELB_HEALTH_CHECK_UNHEALTHY_THRESHOLD = 5
_CC_PROJ_2_2_ELB_HEALTH_CHECK_PING_TARGET = 'HTTP:80/heartbeat'
_CC_PROJ_2_2_ELB_NAME = 'LB-CC-Proj-2-2'
_CC_PROJ_2_2_ELB_AVAIL_ZONE_US_ESAT_1B = 'us-east-1b'
_CC_PROJ_2_2_ELB_LISTENER_HTTP_80_80 = (80, 80, 'http')

_CC_PROJ_2_2_LAUNCH_CONFIGURATION_NAME = 'LC_CC_Proj_2_2'
_CC_PROJ_2_2_KEY_NAME = 'menlopark'
_CC_PROJ_2_2_DC_SECURITY_GROUP = 'HTTP-80-accessible'


# =============================================================================
# @type: function
# @brief: Print the given string to stdout as tracing log.
# @param: [in] msg: The string message to print to stdout.
# @return: N/A
# @note: Later we should use the provided logging utility in Python.

def _log_stdout( msg ) :
    print msg


# =============================================================================
# @type: function
# @brief: Print all the properties in an object.
# @param: [in] obj: The object whose properties are to be enumerated.
# @return: N/A

def _debug_enum_obj_properties( name, obj ) :
    print '\n'
    print "Object: " + name + ": \n"
    for prop, value in vars( obj ).iteritems( ) :
        print '\t' + prop, " = ", value
    print '\n'


# =============================================================================
# @type: function
# @brief: Connect to the specified region
# @param: [in][str] region: The region code.
# @return:
# boto.ec2.EC2Connection: The connection object.
# None: If it failed to connect to the region.
# @note: Please see this page for region name mapping:
# http://docs.aws.amazon.com/general/latest/gr/rande.html

def aws_ec2_connect_to_region( region ) :
    return boto.ec2.connect_to_region( region )


# =============================================================================
# @type: function
# @brief: Connect to the specified region
# @param: [in][str] region: The region code.
# @return:
# boto.ec2.ELBConnection: The connection object.
# None: If it failed to connect to the region.
# @note: Please see this page for region name mapping:
# http://docs.aws.amazon.com/general/latest/gr/rande.html

def aws_ec2_elb_connect_to_region( region ) :
    return boto.ec2.elb.connect_to_region( region )


# =============================================================================
# @type: function
# @brief: Create the security group and add one rule.
# @param: [in] conn: The EC2 connection.
# @param: [in] sg_name, sg_description: The security group name and description.
# @param: [in] rule_protocol: Either TCP, UDP or ICMP.
# @param: [in] rule_port_from, rule_port_to: The accessible port range.
# @param: [in] rule_cidr_ip: The CIDR block you are providing access to.
# @param: [in] delete_if_exists: True means if the security group already exists,
# it will be deleted and try to create again.
# @return:
# boto.ec2.securitygroup.SecurityGroup: The security group object
# None

def _unprotected_aws_ec2_create_security_group(
        _conn, _sg_name, _sg_description,
        _rule_protocol, _rule_port_from, _rule_port_to,
        _rule_cidr_ip ) :
    sg = _conn.create_security_group(
        name = _sg_name,
        description = _sg_description
    )

    sg.authorize( _rule_protocol, _rule_port_from, _rule_port_to, _rule_cidr_ip )

    return sg


def aws_ec2_create_security_group( conn, sg_name, sg_description,
                                   rule_protocol, rule_port_from, rule_port_to,
                                   rule_cidr_ip,
                                   delete_if_exists ) :
    sg = None

    try :
        sg = _unprotected_aws_ec2_create_security_group(
            conn, sg_name, sg_description,
            rule_protocol, rule_port_from, rule_port_to,
            rule_cidr_ip
        )
    except Exception as e :
        _log_stdout( "[ERROR] Failed to create the security group: " + str( e ) )
        if delete_if_exists :
            try :
                conn.delete_security_group( name = sg_name )

                sg = _unprotected_aws_ec2_create_security_group(
                    conn, sg_name, sg_description,
                    rule_protocol, rule_port_from, rule_port_to,
                    rule_cidr_ip
                )
            except Exception as e :
                _log_stdout( "[ERROR] Failed to create the security group: " + str( e ) )
                return None

    aws_ec2_add_resource_tag( conn, sg, _CC_PROJ_2_2_TAG_NAME, _CC_PROJ_2_2_TAG_VALUE )

    return sg


# =============================================================================
# @type: function
# @brief: Create the load balancer for CC-Proj-2.2
# @param: [in] ec2_elb_conn: The ELB connection.
# @return:
#   boto.ec2.elb.loadbalancer.LoadBalancer: The load balancer object.
#   None: In case of failure.

def aws_ec2_elb_create_load_balancer( ec2_elb_conn, security_group ) :

    lb = None

    try :
        # Create health check
        hc = boto.ec2.elb.HealthCheck(
            interval = _CC_PROJ_2_2_ELB_HEALTH_CHECK_INTERVAL,
            healthy_threshold = _CC_PROJ_2_2_ELB_HEALTH_CHECK_HEALTHY_THRESHOLD,
            unhealthy_threshold = _CC_PROJ_2_2_ELB_HEALTH_CHECK_UNHEALTHY_THRESHOLD,
            target = _CC_PROJ_2_2_ELB_HEALTH_CHECK_PING_TARGET
        )

        # Create Load Balancer
        lb = ec2_elb_conn.create_load_balancer(
            name = _CC_PROJ_2_2_ELB_NAME,
            zones = [_CC_PROJ_2_2_ELB_AVAIL_ZONE_US_ESAT_1B],
            listeners = [_CC_PROJ_2_2_ELB_LISTENER_HTTP_80_80],
            security_groups = [security_group.id]
        )

        lb.configure_health_check( hc )
    except Exception as e :
        _log_stdout( str( e ) )

    return lb


# =============================================================================
# @type: function
# @brief: Create the Launch Configuration template for CC-Proj-2.2
# @param: ec2_as_conn: The EC2 Auto Scaling connection.
# @param: delete_if_exists: Delete the launch configuration if it exists.
# @return: LaunchConfiguration object.

def aws_ec2_create_launch_configuration( ec2_as_conn, delete_if_exists ) :

    lc = None

    while True :
        try :
            lc = LaunchConfiguration(
                name = _CC_PROJ_2_2_LAUNCH_CONFIGURATION_NAME,
                image_id = _CC_PROJ_2_2_DC_AMI,
                instance_type = _CC_PROJ_2_2_DC_INSTANCE_TYPE,
                key_name = _CC_PROJ_2_2_KEY_NAME,
                security_groups = [_CC_PROJ_2_2_DC_SECURITY_GROUP]
            )
            ec2_as_conn.create_launch_configuration( lc )

            break

        except Exception as e :
            _log_stdout( str( e ) )
            if delete_if_exists :
                ec2_as_conn.delete_launch_configuration( _CC_PROJ_2_2_LAUNCH_CONFIGURATION_NAME )
            time.sleep( 1 )

    return lc


# =============================================================================
# @type: function
# @brief: Create an Auto Scaling Group for CC-Proj-2.2
# @param: [in] ec2_as_conn: The EC2 Auto Scaling connection.
# @param: [in] load_balancer: The Elastic Load Balancer object.
# @param: [in] ec2_cw_conn: The EC2 CloudWatch object.
# @return: Auto Scaling Group object.

def aws_ec2_autoscale_create_group( ec2_as_conn, lc, load_balancer, ec2_cw_conn ) :

    ag = None

    try :

        # Tag for Auto Scaling Group
        as_tag = boto.ec2.autoscale.tag.Tag(
            key = _CC_PROJ_2_2_TAG_NAME,
            value = _CC_PROJ_2_2_TAG_VALUE,
            propagate_at_launch = True,
            resource_id = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME
        )

        # Create the Auto Scaling Group
        ag = AutoScalingGroup( group_name = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME,
                               load_balancers = [load_balancer.name],
                               availability_zones = ['us-east-1b'],
                               launch_config = lc,
                               min_size = 2,
                               max_size = 10,
                               connection = ec2_as_conn,
                               tags = [as_tag]
        )
        ec2_as_conn.create_auto_scaling_group( ag )

        # Create scaling-up policy
        scale_up_policy = ScalingPolicy(
            name = _CC_PROJ_2_2_SCALE_POLICY_UP_NAME,
            adjustment_type = _CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_TYPE_CHANGE_IN_CAPACITY,
            as_name = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME,
            scaling_adjustment = _CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_UP,
            cooldown = _CC_PROJ_2_2_SCALE_POLICY_COOLDOWN
        )
        ec2_as_conn.create_scaling_policy( scale_up_policy )
        # Refresh the scaling-up policy
        scale_up_policy = ec2_as_conn.get_all_policies(
            as_group = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME,
            policy_names = [_CC_PROJ_2_2_SCALE_POLICY_UP_NAME] )[0]

        # Create scaling-down policy
        scale_down_policy = ScalingPolicy(
            name = _CC_PROJ_2_2_SCALE_POLICY_DOWN_NAME,
            adjustment_type = _CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_TYPE_CHANGE_IN_CAPACITY,
            as_name = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME,
            scaling_adjustment = _CC_PROJ_2_2_SCALE_POLICY_ADJUSTMENT_DOWN,
            cooldown = _CC_PROJ_2_2_SCALE_POLICY_COOLDOWN )
        ec2_as_conn.create_scaling_policy( scale_down_policy )
        # Refresh the scaling-down policy
        scale_down_policy = ec2_as_conn.get_all_policies(
            as_group = _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME,
            policy_names = [_CC_PROJ_2_2_SCALE_POLICY_DOWN_NAME] )[0]

        # Create alarms
        alarm_dimensions = { "AutoScalingGroupName" : _CC_PROJ_2_2_AUTO_SCALING_GROUP_NAME }

        # Create scaling-up alarm
        scale_up_alarm = MetricAlarm(
            name = 'scale_up_on_cpu', namespace = 'AWS/EC2',
            metric = 'CPUUtilization', statistic = 'Average',
            comparison = '>', threshold = '70',
            period = '60', evaluation_periods = 1,
            alarm_actions = [scale_up_policy.policy_arn],
            dimensions = alarm_dimensions
        )
        ec2_cw_conn.create_alarm( scale_up_alarm )

        # Create scaling-down alarm
        scale_down_alarm = MetricAlarm(
            name = 'scale_down_on_cpu', namespace = 'AWS/EC2',
            metric = 'CPUUtilization', statistic = 'Average',
            comparison = '<', threshold = '40',
            period = '60', evaluation_periods = 1,
            alarm_actions = [scale_down_policy.policy_arn],
            dimensions = alarm_dimensions
        )
        ec2_cw_conn.create_alarm( scale_down_alarm )

    except Exception as e :
        _log_stdout( str( e ) )

    return ag


# =============================================================================
# @type: function
# @brief: Add tag to the specified resource.
# @param: [in] conn: The EC2 connection.
# @param: [in] inst: The resource to tag.
# @param: [in] tag_name, tag_value: The tag name and value.
# @return: N/A

def aws_ec2_add_resource_tag( conn, resource, tag_name, tag_value ) :

    conn.create_tags( [resource.id], { tag_name : tag_value } )


# =============================================================================
# @type: function
# @brief: Start an EC2 instance.
# @param: [in] conn: The EC2 connection.
# @param: [in] image: The image name.
# @param: [in] type: The instance type.
# @param: [in] sg: The security group name.
# @return:
#   boto.ec2.instance.Instance: The launched instance.
#   None: The instance was not launched.
# @note: Subnet was considered in the first place but the actual execution
#   of code reports that subnet and security group seem to conflict to
#   each other. So subnet is removed here.

def aws_ec2_start_instance( conn, image, inst_type, sg ) :

    reservation = conn.run_instances(
        image_id = image,
        instance_type = inst_type,
        security_groups = sg,
    )

    if len( reservation.instances ) == 0 :
        return None

    return reservation.instances[0]


# =============================================================================
# @type: function
# @brief: Wait for the instance to be running in order to get the public DNS.
# @param: [in] conn: The EC2 connection.
# @param: [in] inst: The instance to wait for.
# @param: [in] interval: The interval for public DNS query. Must be >= 1.
# @return: str: The public DNS name of inst.

def aws_ec2_wait_for_instance_public_dns_name( conn, inst, interval ) :

    public_dns_name = str()
    query_interval = interval if interval >= 1 else 1

    while not public_dns_name :
        try :
            instances = conn.get_only_instances( [inst.id] )

            if len( instances ) > 0 :
                public_dns_name = instances[0].public_dns_name

        except :
            # We just do nothing for now. Let the program sleep for a while
            # and try again.
            pass

        time.sleep( query_interval )

    return public_dns_name


# =============================================================================
# @type: function
# @brief: Delete the specified Launch Configuration object.
# @param: [in] ec2_as_conn: The EC2 Auto Scaling connection.
# @param: [in] lc: The Launch Configuration object to be deleted.
# @param: [in] retries: How many times we should retry on deletion.
# @param: [in] retry_interval: The interval between two retries.
# @return: boolean:
#   True: The Launch Configuration object was deleted successfully.
#   False: The Launch Configuration object was not deleted successfully.

def aws_ec2_delete_launch_configuration( ec2_as_conn, lc, retries, retry_interval ) :

    deleted = False

    for i in range( 0, retries ) :
        try :
            ec2_as_conn.delete_launch_configuration( lc.name )
            _log_stdout( "[INFO] Resource \"" + str( lc ) + "\" is deleted." )
            deleted = True
            break
        except Exception as e :
            _log_stdout( "[ERROR] Resource deletion failed: " + str( e ) )
            time.sleep( retry_interval )

    return deleted


# =============================================================================
# @type: function
# @brief: Delete a resource that supports delete() method.
# @param: [in] resource: The resource to be deleted.
# @param: [in] retries: How many times we should retry on deletion.
# @param: [in] retry_interval: The interval between two retries.
# @return: boolean:
#   True: The resource was deleted successfully.
#   False: The resource was not deleted successfully.

def aws_ec2_delete_resource( resource, retries, retry_interval ) :

    deleted = False

    if resource is None :
        return False

    for i in range( 0, retries ) :
        try :
            resource.delete()
            _log_stdout( "[INFO] Resource \"" + str( resource ) + "\" is deleted." )
            deleted = True
            break
        except Exception as e :
            _log_stdout( "[ERROR] Resource \"" + str( resource ) + "\" deletion failed: " + str( e ) )
            time.sleep( retry_interval )

    return deleted


# =============================================================================
# @type: function
# @brief: Parse the testing log to see the current progress.
# @param: [in] log_content: The log content of the current Junior-System-Architect
#   testing.
# @return: str(): The current progress in form of "Minute XX".

def _parse_log_for_current_progress( log_content ) :

    last_rps_section_name = _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_UNKNOWN_TEST_PROGRESS

    config = ConfigParser.ConfigParser()
    config.readfp( io.BytesIO( log_content ) )

    section_names = config.sections()

    # Find all the sections that are in the specified pattern. They are the
    #   sections that contain RPS data and reflect the current progress.
    rps_section_names = list()
    for section_name in section_names :
        if re.match( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_RPS_LOG_SECTION_NAME_PATTERN,
                     section_name ) :
            rps_section_names.append( section_name )

    # We assume that the latest progress is reflected in the latest section.
    rps_sec_num = len( rps_section_names )
    if rps_sec_num > 0 :
        last_rps_section_name = rps_section_names[rps_sec_num-1]

    return last_rps_section_name


# =============================================================================
# @type: function
# @brief: Parse the test log to see if the testing is completed or not.
# @param: [in] log_content: The textual content of the log file.
# @return: boolean:
#   True: The Junior-System-Architect testing is completed.
#   False: The Junior-System-Architect testing is not completed.

def _parse_log_for_test_completion_flag( log_content ) :

    test_completed = False

    config = ConfigParser.ConfigParser()
    config.readfp( io.BytesIO( log_content ) )

    section_names = config.sections()

    # Find the test completion flag.
    for section_name in section_names :
        if section_name == _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_TEST_END :
            test_completed = True

    return test_completed


# =============================================================================
# @type: function
# @brief: Generate the full URL of the test log file.
# @param: [in] dns_lg: The DNS of the Load Generator(LG) instance.
# @param: [in] test_log_name: The name of the test log file.

def _generate_test_log_url( dns_lg, test_log_name ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_LOG_URL_MIDDLE +
             test_log_name )


# =============================================================================
# @type: function
# @brief: Parse the given HTML content to get the test log URL.
# @param: [in] html_content: The HTML content returned from the HTTP GET
#   request from the Load Generator instance.
# @return: str: The full URL of test log.
# @note: We search for the 'test.[\d]*.log' pattern.

def _get_test_log_name( html_content ) :

    log_file_names = re.findall(
        _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_LOG_FILE_NAME_PATTERN,
        html_content
    )

    if len( log_file_names ) == 0 :
        return str()

    return log_file_names[0]    # There should be only one occurrence.


# =============================================================================
# @type: function
# @brief: Generate the URL of Load Generator instance.
# @param: [in] dns_lg: The DNS of Load Generator(LG) instance.
# @return: str: The full URL of the Load Generator instance.

def _generate_load_generator_url( dns_lg ) :

    return _AWS_EC2_INSTANCE_URL_HTTP_PREFIX + dns_lg


# =============================================================================
# @type: function
# @brief: Given the Load Generator and Load Balancer's DNS names, generate the
#   full URL for warm-up.
# @param: [in] dns_lg: The DNS name of the Load Generator instance.
# @param: [in] dns_lb: The DNS name of the Load Balancer.
# @return: str: The full URL string.
def _generate_warmup_url( dns_lg, dns_lb ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_2_WARM_UP_URL_MIDDLE +
             dns_lb
    )


# =============================================================================
# @type: function
# @brief: Given the Load Generator and Load Balancer's DNS names, generate the
#   full URL for junior-system-architect testing.
# @param: [in] dns_lg: The DNS name of the Load Generator instance.
# @param: [in] dns_lb: The DNS name of the Load Balancer.
# @return: str: The full URL string.

def _generate_junior_sys_architect_testing_url( dns_lg, dns_lb ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_START_TESTING_URL_MIDDLE +
             dns_lb
    )


# =============================================================================
# @type: function
# @brief: The main work flow.
# @param: [in] args: The command line arguments.
# @return: N/A
# @note: args
#   args[0]: The name of this script.
#   args[1]: The Load Generator DNS name.

def main( args ) :

    if len( args ) < 2 :
        _log_stdout( "[ERROR] Missing argument: Load Generator DNS name." )
        return

    dns_lg = args[1]    # args[1] is the Load Generator DNS name

    # -------------------------------------------------------------------------

    # Step 0: First of first, establish various kinds of connections.
    ec2_conn = aws_ec2_connect_to_region( _AWS_EC2_REGION_NORTH_VIRGINIA )
    if ec2_conn is None :
        _log_stdout( "[ERROR] AWS EC2 connection failed: " + _AWS_EC2_REGION_NORTH_VIRGINIA )
        return
    else :
        _log_stdout( "[INFO] AWS EC2 connection established: " + str( ec2_conn ) )

    ec2_elb_conn = aws_ec2_elb_connect_to_region( _AWS_EC2_REGION_NORTH_VIRGINIA )
    if ec2_elb_conn is None :
        _log_stdout( "[ERROR] AWS EC2 ELB connection failed: " + _AWS_EC2_REGION_NORTH_VIRGINIA )
        return
    else :
        _log_stdout( "[INFO] AWS EC2 ELB connection established: " + str( ec2_elb_conn ) )

    ec2_as_conn = boto.ec2.autoscale.connect_to_region( _AWS_EC2_REGION_NORTH_VIRGINIA )
    if ec2_as_conn is None :
        _log_stdout( "[ERROR] AWS EC2 Auto Scaling connection failed: " + _AWS_EC2_REGION_NORTH_VIRGINIA )
        return
    else :
        _log_stdout( "[INFO] AWS EC2 Auto Scaling connection established: " + str( ec2_as_conn ) )

    ec2_cw_conn = boto.ec2.cloudwatch.connect_to_region( _AWS_EC2_REGION_NORTH_VIRGINIA )
    if ec2_cw_conn is None :
        _log_stdout( "[ERROR] AWS EC2 CloudWatch connection failed: " + _AWS_EC2_REGION_NORTH_VIRGINIA )
        return
    else :
        _log_stdout( "[INFO] AWS EC2 CloudWatch connection established: " + str( ec2_cw_conn ) )

    # -------------------------------------------------------------------------

    # Step 1: Launch the Load Generator and add tag.
    # This step will be done manually.

    # -------------------------------------------------------------------------

    # Step 2: Create the security group.
    # A Security Group to allow ALL requests on ALL ports from ANYWHERE and
    # allow all outgoing requests on all ports to anywhere.
    sg_free4all = aws_ec2_create_security_group(
        conn = ec2_conn,
        sg_name = _CC_PROJ_2_2_SECURITY_GROUP_NAME,
        sg_description = _CC_PROJ_2_2_SECURITY_GROUP_DESCRIPTION,
        rule_protocol = _CC_PROJ_2_2_SG_RULE_PROTOCOL,
        rule_port_from = _CC_PROJ_2_2_SG_RULE_PORT_FROM,
        rule_port_to = _CC_PROJ_2_2_SG_RULE_PORT_TO,
        rule_cidr_ip = _CC_PROJ_2_2_SG_RULE_CIDR_IP,
        delete_if_exists = True
    )
    if sg_free4all is None :
        _log_stdout( "[ERROR] Security group " + _CC_PROJ_2_2_SECURITY_GROUP_NAME + " was not created." )
    else :
        _log_stdout( "[INFO] Security group " + _CC_PROJ_2_2_SECURITY_GROUP_NAME + " was created." )

    # Step 3: Create an Elastic Load Balancer(ELB).
    load_balancer = aws_ec2_elb_create_load_balancer( ec2_elb_conn, sg_free4all )
    if load_balancer is None :
        _log_stdout( "[ERROR] AWS EC2 Load Balancer was not created" )
    else :
        _log_stdout( "[INFO] AWS EC2 Load Balancer was created successfully." )

    # Step 4: Create a Launch Configuration template
    lc = aws_ec2_create_launch_configuration( ec2_as_conn, delete_if_exists = True )
    if lc is None :
        _log_stdout( "[ERROR] AWS EC2 Launch Configuration was not created." )
    else :
        _log_stdout( "[INFO] AWS EC2 Launch Configuration was created successfully." )

    # Step 5: Create the Auto Scaling Group
    ag = aws_ec2_autoscale_create_group( ec2_as_conn, lc, load_balancer, ec2_cw_conn )
    if ag is None :
        _log_stdout( "[ERROR] AWS EC2 Auto Scaling Group was not created" )
    else :
        _log_stdout( "[INFO] AWS EC2 Auto Scaling Group was created successfully." )

    # -------------------------------------------------------------------------

    # Step 6: Warm up the Load Balancer
    warmup_url = _generate_warmup_url( dns_lg, load_balancer.dns_name )
    # Warm up the ELB multiple times to make it fully engaged.
    for i in range(0, _CC_PROJ_2_2_WARM_UP_TRIES ) :
        while True :
            try :
                _log_stdout( "[INFO] Trying to start the ELB warm-up at: " + warmup_url )
                urllib2.urlopen( warmup_url )
                _log_stdout( "[INFO] ELB warm-up started." )
                break
            except Exception as e :
                _log_stdout( "[INFO] ELB warm-up failed to start because: " + str( e ) )

            _log_stdout( "[INFO] Sleep for " +
                str( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_CHECK_AVAILABILITY_INTERVAL ) +
                " second(s) and try again later..."
            )
            time.sleep( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_CHECK_AVAILABILITY_INTERVAL )

        _log_stdout( "[INFO] Wait for 6 minutes for the Load Balancer warm-up" )
        time.sleep( _CC_PROJ_2_2_WARM_UP_TESTING_WAIT_DURATION )

    # -------------------------------------------------------------------------

    # Step 7: Start the junior system architect testing
    jsat_url = _generate_junior_sys_architect_testing_url( dns_lg, load_balancer.dns_name )

    test_log_name = str()
    while True :
        try :
            _log_stdout( "[INFO] Trying to start the junior-system-architect testing at: " + jsat_url )
            html_content = urllib2.urlopen( jsat_url ).read()
            # Parse the content to get the test log URL.
            test_log_name = _get_test_log_name( html_content )
            if test_log_name :
                _log_stdout( "[INFO] Junior System Architect testing started. Log file: " + test_log_name )
                break
            else :
                _log_stdout( "[INFO] Junior System Architect testing has not been started. "
                             "Log file name is empty."
                )
        except Exception as e :
            _log_stdout( "[INFO] Junior System Architect testing failed to start because: " + str( e ) )

        _log_stdout( "[INFO] Sleep for " +
                    str( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_CHECK_AVAILABILITY_INTERVAL ) +
                    " second(s) and try again later..."
        )
        time.sleep( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_CHECK_AVAILABILITY_INTERVAL )

    # -------------------------------------------------------------------------

    # Step 8: Start to monitor the test completion flag.
    test_completed = False

    while not test_completed :

        # Sleep for a while to allow the testing running.
        _log_stdout( "[INFO] Sleep for " +
                     str( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_COMPLETION_CHECK_INTERVAL ) +
                     " seconds to allow the testing running..." )
        time.sleep( _CC_PROJ_2_2_JUNIOR_SYS_ARCHITECT_TESTING_COMPLETION_CHECK_INTERVAL )

        # Now wake up and check the current testing completion status.

        # Get the current progress by parsing the log.
        test_log_url = _generate_test_log_url( dns_lg, test_log_name )
        log_content = urllib2.urlopen( test_log_url ).read()
        test_completed = _parse_log_for_test_completion_flag( log_content)

        if not test_completed :
            current_progress = _parse_log_for_current_progress( log_content )
            _log_stdout( "[INFO] Current progress: " + str( current_progress ) )
        else:
            _log_stdout( "[INFO] Junior System Architect testing is finished. Current log:" )
            _log_stdout( log_content )

    # -------------------------------------------------------------------------

    # Step 9: Shutting down and release all the resources previously allocated.

    # Shutdown all the instances associated with the auto scaling group
    #   before releasing the auto scaling group resource.
    _log_stdout( "[INFO] Start shutting down the Data Center instances..." )
    ag.shutdown_instances()

    _log_stdout( "[INFO] Wait for " +
                 str( _CC_PROJ_2_2_AUTO_SCALE_INSTANCE_SHUT_DOWN_TIME ) +
                 " seconds to allow instances shut down..." )
    time.sleep( _CC_PROJ_2_2_AUTO_SCALE_INSTANCE_SHUT_DOWN_TIME )

    aws_ec2_delete_resource(
        ag,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRIES,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRY_INTERVAL
    )

    aws_ec2_delete_launch_configuration(
        ec2_as_conn, lc,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRIES,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRY_INTERVAL
    )

    aws_ec2_delete_resource(
        load_balancer,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRIES,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRY_INTERVAL
    )

    aws_ec2_delete_resource(
        sg_free4all,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRIES,
        _CC_PROJ_2_2_RESOURCE_DELETION_RETRY_INTERVAL
    )


# =============================================================================
# @type: script
# @brief: The entry point of the script.

if __name__ == "__main__" :
    # Log the starting time
    _log_stdout( "[INFO] Start time: " + str( datetime.datetime.now( ) ) )

    main( sys.argv )

    # Log the ending time
    _log_stdout( "[INFO] End time: " + str( datetime.datetime.now( ) ) )