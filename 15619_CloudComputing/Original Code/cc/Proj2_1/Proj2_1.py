# =============================================================================
# @type: file
# @brief: Cloud Computing: Project 2.1(CC-Proj-2.1)
# @author: robin
# @email: yaobinw@andrew.cmu.edu
# @note:
#   1). From this project, I will increasingly apply Google's Python coding
#   style. However, being a novice programmer to Python, there are still many
#   language features that I don't know so the current coding style may not
#   benefit every language feature, such as doc string.
#   2). Later the PEP8 coding style will be considered.
#   3). Currently, the following Google Python coding styles are violated
#   intentionally because I think mine are better than theirs:
#       a). Blank spaces are used after opening parenthesis "(" and before
#       the closing parenthesis ")".
#       b). Add a blank space before colon.
#       c). Add blank spaces before and after "=".
#   4). All the other violations are due to my unfamiliarity to Python and
#   will be corrected later.


# =============================================================================
# @type: directive
# @brief: Imports.

import urllib2
import re
import ConfigParser
import io
import time
import datetime
import boto.ec2
import boto.exception


# =============================================================================
# @type: constant
# @brief: AWS EC2 constants

_AWS_EC2_REGION_NORTH_VIRGINIA = "us-east-1"
_AWS_EC2_INSTANCE_URL_HTTP_PREFIX = "http://"


# =============================================================================
# @type: constant
# @brief: CC-Proj-2.1-related constants

_CC_PROJ_2_1_HORIZONTAL_TESTING_LG_AMI = "ami-4c4e0f24"   # LG = Load Generator
_CC_PROJ_2_1_HORIZONTAL_TESTING_DC_AMI = "ami-b04106d8"   # DC = Data Center
# _CC_PROJ_2_1_HORIZONTAL_TESTING_KEY_NAME = "menlopark"

_CC_PROJ_2_1_TAG_NAME = "Project"
_CC_PROJ_2_1_TAG_VALUE = "2.1"
_CC_PROJ_2_1_RESOURCE_TAG = {
    _CC_PROJ_2_1_TAG_NAME : _CC_PROJ_2_1_TAG_VALUE
}


_CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_NAME = "sg_CC_Proj_2_1"
_CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_DESCRIPTION = \
    "Security Group for CC-Proj-2.1: HTTP 80 accessible."
_CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUPS = [
    _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_NAME
]

_CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PROTOCOL = "TCP"
_CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PORT_FROM = 80
_CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PORT_TO = 80
_CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_CIDR_IP = "0.0.0.0/0"

_CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_STATE_QUERY_INTERVAL = 5
_CC_PROJ_2_1_HORIZONTAL_TESTING_CHECK_AVAILABILITY_INTERVAL = 30
_CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_ADDITION_INTERVAL_LIMIT = 100
_CC_PROJ_2_1_HORIZONTAL_TESTING_RPS_CHECK_INTERVAL = \
    _CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_ADDITION_INTERVAL_LIMIT + 20   # Add 20 more seconds


# The Load Generator instance type for CC-Proj-2.1 in horizontal testing.
_CC_PROJ_2_1_VERTICAL_TESTING_LG_TYPE = "m3.medium"
# The Data Center instance type for CC-Proj-2.1 in horizontal testing.
_CC_PROJ_2_1_VERTICAL_TESTING_DC_TYPE = "m3.medium"
# The target RPS of CC-Proj-2.1 in horizontal testing.
_CC_PROJ_2_1_VERTICAL_TESTING_TARGET_RPS = 4000

_CC_PROJ_2_1_HORIZONTAL_TESTING_LOG_URL_MIDDLE = "/log?name="
_CC_PROJ_2_1_HORIZONTAL_TESTING_LOG_FILE_NAME_PATTERN = "test\.[\d]*\.log"
_CC_PROJ_2_1_HORIZONTAL_TESTING_ADD_INSTANCE_URL_MIDDLE = "/test/horizontal/add?dns="
_CC_PROJ_2_1_HORIZONTAL_TESTING_START_TESTING_URL_MIDDLE = "/test/horizontal?dns="
_CC_PROJ_2_1_HORIZONTAL_TESTING_RPS_LOG_SECTION_NAME_PATTERN = "Minute\ \d+"


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
# @brief: Generate the URL of Load Generator instance.
# @param: [in] dns_lg: The DNS of Load Generator(LG) instance.
# @return: str: The full URL of the Load Generator instance.

def _generate_load_generator_url( dns_lg ) :

    return _AWS_EC2_INSTANCE_URL_HTTP_PREFIX + dns_lg


# =============================================================================
# @type: function
# @brief: Given the Load Generator and Data Center's DNS names, generate the
#   full URL for horizontal testing.
# @param: [in] dns_lg: The DNS name of the Load Generator instance.
# @param: [in] dns_dc: The DNS name of the Data Center instance.
# @return: str: The full URL string.

def _generate_horizontal_testing_url( dns_lg, dns_dc ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_1_HORIZONTAL_TESTING_START_TESTING_URL_MIDDLE +
             dns_dc
    )


# =============================================================================
# @type: function
# @brief: Generate the URL for adding an instance to the horizontal testing.
# @param: [in] dns_lg: The DNS of the Load Generator instance.
# @param: [in] dns_dc_new: The DNS of the new Data Center instance.
# @return: str: The full URL string of adding the new Data Center instance
#   to the horizontal testing being executed by the Load Generator instance.

def _generate_horizontal_testing_add_instance_url( dns_lg, dns_dc_new ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_1_HORIZONTAL_TESTING_ADD_INSTANCE_URL_MIDDLE +
             dns_dc_new
    )


# =============================================================================
# @type: function
# @brief: Parse the given HTML content to get the test log URL.
# @param: [in] html_content: The HTML content returned from the HTTP GET
#   request from the Load Generator instance.
# @return: str: The full URL of test log.
# @note: We search for the 'test.[\d]*.log' pattern.

def _get_test_log_name( html_content ) :

    log_file_names = re.findall(
        _CC_PROJ_2_1_HORIZONTAL_TESTING_LOG_FILE_NAME_PATTERN,
        html_content
    )

    if len( log_file_names ) == 0 :
        return str()

    return log_file_names[0]    # There should be only one occurrence.


# =============================================================================
# @type: function
# @brief: Generate the full URL of the test log file.
# @param: [in] dns_lg: The DNS of the Load Generator(LG) instance.
# @param: [in] test_log_name: The name of the test log file.


def _generate_test_log_url( dns_lg, test_log_name ) :

    return ( _generate_load_generator_url( dns_lg ) +
             _CC_PROJ_2_1_HORIZONTAL_TESTING_LOG_URL_MIDDLE +
             test_log_name )


# =============================================================================
# @type: function
# @brief: Parse the test log to calculate the current RPS.
# @param: [in] log_content: The textual content of the log file.
# @return: float: The current total RPS of all the Data Center instances.

def _parse_log_for_current_rps( log_content ) :

    rps_total = 0.0

    config = ConfigParser.ConfigParser()
    config.readfp( io.BytesIO( log_content ) )

    section_names = config.sections()

    # Find all the sections that are in the specified pattern. They are the
    #   sections that contain RPS data.
    rps_section_names = list()
    for section_name in section_names :
        if re.match( _CC_PROJ_2_1_HORIZONTAL_TESTING_RPS_LOG_SECTION_NAME_PATTERN,
                     section_name ) :
            rps_section_names.append( section_name )

    # We assume that the last RPS section is the latest one.
    rps_sec_num = len( rps_section_names )
    if rps_sec_num > 0 :
        last_rps_section_name = rps_section_names[rps_sec_num-1]

        option_names = config.options( last_rps_section_name )

        for option_name in option_names :
            rps_inst = config.getfloat( last_rps_section_name, option_name )
            rps_total += rps_inst

    return rps_total


# =============================================================================
# @type: function
# @brief: Add tag to the specified instance.
# @param: [in] conn: The EC2 connection.
# @param: [in] inst: The instance to tag.
# @param: [in] tag_name, tag_value: The tag name and value.
# @return: N/A

def aws_ec2_add_instance_tag( conn, inst, tag_name, tag_value ) :

    conn.create_tags( [inst.id], { tag_name : tag_value } )


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
# @brief: Create the security group and add one rule.
# @param: [in] conn: The EC2 connection.
# @param: [in] sg_name, sg_description: The security group name and description.
# @param: [in] rule_protocol: Either TCP, UDP or ICMP.
# @param: [in] rule_port_from, rule_port_to: The accessible port range.
# @param: [in] rule_cidr_ip: The CIDR block you are providing access to.
# @param: [in] delete_if_exists: True means if the security group already exists,
#   it will be deleted and try to create again.
# @return: N/A

def aws_ec2_create_security_group( conn, sg_name, sg_description,
                                   rule_protocol, rule_port_from, rule_port_to,
                                   rule_cidr_ip,
                                   delete_if_exists ) :

    def _unprotected_aws_ec2_create_security_group(
            _conn, _sg_name, _sg_description,
            _rule_protocol, _rule_port_from, _rule_port_to,
            _rule_cidr_ip) :
        sg = _conn.create_security_group(
            name = _sg_name,
            description = _sg_description
        )

        sg.authorize( _rule_protocol, _rule_port_from, _rule_port_to, _rule_cidr_ip )

    try :
        _unprotected_aws_ec2_create_security_group(
            conn, sg_name, sg_description,
            rule_protocol, rule_port_from, rule_port_to,
            rule_cidr_ip
        )
    except :
        if delete_if_exists :
            conn.delete_security_group( name = sg_name )

            _unprotected_aws_ec2_create_security_group(
                conn, sg_name, sg_description,
                rule_protocol, rule_port_from, rule_port_to,
                rule_cidr_ip
            )


# =============================================================================
# @type: function
# @brief: Connect to the specified region
# @param: [in][str] region: The region code.
# @return:
#   boto.ec2.EC2Connection: The connection object.
#   None: If it failed to connect to the region.
# @note: Please see this page for region name mapping:
#   http://docs.aws.amazon.com/general/latest/gr/rande.html

def aws_ec2_connect_to_region( region ) :

    return boto.ec2.connect_to_region( region )


# =============================================================================
# @type: function
# @brief: The main work flow.
# @param: N/A
# @return: N/A

def main():

    # Variable: Flag of whether the RPS requirement is satisfied.
    rps_satisfied = False

    # Step 1: Connect to AWS
    ec2_conn = aws_ec2_connect_to_region( _AWS_EC2_REGION_NORTH_VIRGINIA )
    _log_stdout( "AWS EC2 connection established: " + str( ec2_conn ) )

    # Step 2: Create security group
    aws_ec2_create_security_group(
        conn = ec2_conn,
        sg_name = _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_NAME,
        sg_description = _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_DESCRIPTION,
        rule_protocol = _CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PROTOCOL,
        rule_port_from = _CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PORT_FROM,
        rule_port_to = _CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_PORT_TO,
        rule_cidr_ip = _CC_PROJ_2_1_HORIZONTAL_TESTING_SG_RULE_CIDR_IP,
        delete_if_exists = True
    )
    _log_stdout( "Security group created: name = " +
                 _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUP_NAME )

    # Step 3: Create the Load Generator instance.
    inst_lg = aws_ec2_start_instance(
        conn = ec2_conn,
        image = _CC_PROJ_2_1_HORIZONTAL_TESTING_LG_AMI,
        inst_type = _CC_PROJ_2_1_VERTICAL_TESTING_LG_TYPE,
        sg = _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUPS
    )
    _log_stdout( "Instance(LG) started: ID = " + str( inst_lg.id ) )

    # Wait until the instance is actually running and return the
    #   public DNS name.
    dns_lg = aws_ec2_wait_for_instance_public_dns_name(
        ec2_conn, inst_lg,
        _CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_STATE_QUERY_INTERVAL
    )
    _log_stdout( "Instance(LG) public DNS available: " + str( dns_lg ) )

    # Add tags to instance
    aws_ec2_add_instance_tag( ec2_conn, inst_lg, _CC_PROJ_2_1_TAG_NAME, _CC_PROJ_2_1_TAG_VALUE )
    _log_stdout( "Instance(LG) tagged: " + str( {_CC_PROJ_2_1_TAG_NAME : _CC_PROJ_2_1_TAG_VALUE} ) )

    # Step 4: Create the Data Center instance.
    inst_dc = aws_ec2_start_instance(
        conn = ec2_conn,
        image = _CC_PROJ_2_1_HORIZONTAL_TESTING_DC_AMI,
        inst_type = _CC_PROJ_2_1_VERTICAL_TESTING_DC_TYPE,
        sg = _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUPS
    )
    _log_stdout( "Instance(DC) started: ID = " + str( inst_dc.id ) )

    # Wait until the instance is actually running
    dns_dc = aws_ec2_wait_for_instance_public_dns_name(
        ec2_conn, inst_dc,
        _CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_STATE_QUERY_INTERVAL
    )
    _log_stdout( "Instance(DC) public DNS available: " + str( dns_dc ) )

    # Add tags to instance
    aws_ec2_add_instance_tag( ec2_conn, inst_dc, _CC_PROJ_2_1_TAG_NAME, _CC_PROJ_2_1_TAG_VALUE )
    _log_stdout( "Instance(DC) tagged: " + str( {_CC_PROJ_2_1_TAG_NAME : _CC_PROJ_2_1_TAG_VALUE} ) )

    # Step 5: Start the horizontal testing
    ht_url = _generate_horizontal_testing_url( dns_lg, dns_dc )

    test_log_name = str()
    while True :
        try :
            _log_stdout( "Trying to start the horizontal testing at: " + ht_url )
            html_content = urllib2.urlopen( ht_url ).read()
            # Parse the content to get the test log URL.
            test_log_name = _get_test_log_name( html_content )
            if test_log_name :
                _log_stdout( "Horizontal testing started. Log file: " + test_log_name )
                break
            else :
                _log_stdout( "Horizontal testing has not been started. "
                             "Log file name is empty."
                )
        except Exception as e :
            _log_stdout( "Horizontal testing failed to start because: " + str( e ) )
            pass

        _log_stdout( " Sleep for " +
                    str( _CC_PROJ_2_1_HORIZONTAL_TESTING_CHECK_AVAILABILITY_INTERVAL ) +
                    " second(s) and try again later..."
        )
        time.sleep( _CC_PROJ_2_1_HORIZONTAL_TESTING_CHECK_AVAILABILITY_INTERVAL )

    # Step 5: Start to monitor the RPS.
    # The URL of the previous horizontal test instance to be added.
    previous_failed_ht_add_inst_url = str()

    while not rps_satisfied :

        _log_stdout( "Sleep for " +
                     str( _CC_PROJ_2_1_HORIZONTAL_TESTING_RPS_CHECK_INTERVAL ) +
                     " seconds..." )
        # There is a 100-second limit to prevent adding Data Center instances
        #   too quickly. Because we had already added the initial Data Center
        #   before this while loop, we need to wait for at least 100 seconds
        #   before adding the next instance.
        time.sleep( _CC_PROJ_2_1_HORIZONTAL_TESTING_RPS_CHECK_INTERVAL )

        # Now wake up and check the current RPS.

        # Get the current RPS by parsing the log.
        test_log_url = _generate_test_log_url( dns_lg, test_log_name )
        log_content = urllib2.urlopen( test_log_url ).read()
        rps_current = _parse_log_for_current_rps( log_content )

        _log_stdout( "Current total RPS: " + str( rps_current ) )

        if rps_current < _CC_PROJ_2_1_VERTICAL_TESTING_TARGET_RPS :

            _log_stdout( "Total RPS is not high enough. "
                         "Will try to start another Data Center instance." )

            # We need to check if the previous instance was added successfully,
            #   in case there was any error. So if the previous instance was
            #   not added successfully, we will not start new instance. We will
            #   try to add it again.
            if len( previous_failed_ht_add_inst_url ) == 0 :

                # Start a new Data Center instance.
                inst_dc_new = aws_ec2_start_instance(
                    conn = ec2_conn,
                    image = _CC_PROJ_2_1_HORIZONTAL_TESTING_DC_AMI,
                    inst_type = _CC_PROJ_2_1_VERTICAL_TESTING_DC_TYPE,
                    sg = _CC_PROJ_2_1_HORIZONTAL_TESTING_SECURITY_GROUPS
                )
                _log_stdout( "New instance(DC) started: ID = " + str( inst_dc_new.id ) )

                # Wait until the instance is actually running
                dns_dc_new = aws_ec2_wait_for_instance_public_dns_name(
                    ec2_conn, inst_dc_new,
                    _CC_PROJ_2_1_HORIZONTAL_TESTING_INSTANCE_STATE_QUERY_INTERVAL
                )
                _log_stdout( "New instance(DC) public DNS available: " + str( dns_dc_new ) )

                # Add tags to instance
                aws_ec2_add_instance_tag(
                    ec2_conn, inst_dc_new,
                    _CC_PROJ_2_1_TAG_NAME, _CC_PROJ_2_1_TAG_VALUE
                )
                _log_stdout( "New instance(DC) tagged: " + str( {_CC_PROJ_2_1_TAG_NAME : _CC_PROJ_2_1_TAG_VALUE} ) )

                ht_add_inst_url = _generate_horizontal_testing_add_instance_url( dns_lg, dns_dc_new )
            else :
                _log_stdout( "There was a previous instance not added to the horizontal testing successfully."
                             "Will try to add that instance again. \n"
                             "URL: " + previous_failed_ht_add_inst_url
                )
                # If we have a previous failed instance to add, then use that URL.
                ht_add_inst_url = previous_failed_ht_add_inst_url

            try :
                _log_stdout( "Horizontal Testing: Try to add new instance: " + ht_add_inst_url )
                urllib2.urlopen( ht_add_inst_url )
                # If the instance is added successfully(no exception), then
                #   we will not have any "previously failed" instance to add
                #   in the next round.
                previous_failed_ht_add_inst_url = str()

                _log_stdout( "New instance added successfully" )
            except Exception as e:
                _log_stdout( "New instance failed to add: " + str( e ) )
                _log_stdout( "Will try again later..." )
                # If the instance is not added(with exception), then we
                #   should keep its URL and try again next time.
                previous_failed_ht_add_inst_url = ht_add_inst_url

        else:
            rps_satisfied = True

            _log_stdout( "Total RPS requirement is satisfied. Current log:" )
            _log_stdout( log_content )

    # Now the RPS is satisfied.
    # DO NOT stop the instances because we need them in the next part of this
    #   project.


# =============================================================================
# @type: script
# @brief: The entry point of the script.

if __name__ == "__main__" :

    _log_stdout( "Start time: " + str( datetime.datetime.now() ) )
    main()
    _log_stdout( "End time: " + str( datetime.datetime.now() ) )