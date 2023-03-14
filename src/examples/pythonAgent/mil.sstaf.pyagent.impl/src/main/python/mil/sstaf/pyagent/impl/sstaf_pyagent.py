import sys


def decompose_command(s):
    fields = s.split(' ')
    num_args = len(fields)
    seq_num = int(fields[0])
    if num_args >= 2:
        command = fields[1]
        args = fields[2:]
    else:
        command = "error"
        args = fields[2:]
        args[0] = f"Poorly formed command '{s}'"

    return seq_num, command, args


def send_result(msg):
    output = f"{msg}"
    #
    # Strip any embedded \n's from the output so it is all on one
    # space-delineated line.
    #
    clean = output.replace("\n", " ")
    sys.stdout.write(clean + '\n')
    sys.stdout.flush()


def send_error(seq_num, msg, command):
    send_result(f"{seq_num} command:{command} error:{msg}")


def make_bad_command_error(fields, msg):
    """
    Make an error message
    :param fields: the arguments to the invocation
    :param msg: the error message
    :return: the error message
    """
    err_msg = f"error: {msg}"
    return err_msg


#
# Lame example method
#
def count_letters(args):
    count = 0
    for arg in args:
        count += len(arg)
    return count


t_zero = 0

def set_t_zero(args):
    global t_zero
    t_zero = int(args[0])
    return t_zero


def compute_capability(args):
    t_now = int(args[0])
    delta_t = t_now - t_zero
    max_t = 18 * 3600 * 1000

    capability = 1.0
    if t_now <= t_zero:
        capability = 1.0
    else:
        capability = 1 - (float(delta_t) / float(max_t))
    return capability


def main():
    #
    # Enter the main command processing loop.
    #
    input = sys.stdin.readline().strip()

    while input not in ['break', 'quit']:
        seq_num, command, args = decompose_command(input)
        #
        # Dispatch to appropriate method
        # I'm sure there is a more Pythonic way.
        #
        try:
            if command == "count":
                c = count_letters(args)
                result = str(c)
                send_result(f"{seq_num} ok count {len(result)} {result}")
            elif command == "setTZero":
                t0 = set_t_zero(args)
                result = str(t0)
                send_result(f"{seq_num} ok setTZero {len(result)} {result}")
            elif command == "advanceClock":
                capability = compute_capability(args)
                result = str(capability)
                send_result(f"{seq_num} ok advanceClock {len(result)} {result}")
            else:
                send_error(seq_num, f"Unknown command '{command}'")
        except Exception as err:
            send_error(seq_num, err, input)
        #
        # Read the next line.
        #
        input = sys.stdin.readline().strip()


if __name__ == "__main__":
    main()
