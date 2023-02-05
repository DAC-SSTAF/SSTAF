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


def send_error(seq_num, msg):
    send_result(f"{seq_num} error {msg}")


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


def main():
    #
    # Enter the main command processing loop.
    #
    s = sys.stdin.readline().strip()
    while s not in ['break', 'quit']:
        seq_num, command, args = decompose_command(s)
        #
        # Dispatch to appropriate method
        # I'm sure there is a more Pythonic way.
        #
        try:
            if command == "count":
                c = count_letters(args)
                result = str(c)
                send_result(f"{seq_num} ok count {len(result)} {result}")
            else:
                send_error(seq_num, f"Unknown command '{command}'")
        except Exception as err:
            send_error(seq_num, err)
        #
        # Read the next line.
        #
        s = sys.stdin.readline().strip()


if __name__ == "__main__":
    main()
