import os, os.path
import sys
import shutil
import subprocess

try:
    from play.utils import package_as_war
    PLAY10 = False
except ImportError:
    PLAY10 = True

MODULE = 'mdemo'

COMMANDS = ['mdemo:gen', 'mdemo:regen']

#execute then use commend like this 'play mdemo:regen'
def execute(**kargs):
    print "execute........"
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    #print "demo command: " + command
    if command == 'mdemo:gen':
        run(app, 'gen')

    if command == 'mdemo:regen':
        run(app, 'regen')

def run(app, cmd):
    print "run............."
    app.check()
    java_cmd = app.java_cmd(['-Xmx64m'], className='com.pxl.demo.DemoCommends', args=[cmd])
#    print java_cmd                                                                                              
    subprocess.call(java_cmd, env=os.environ)
    print

#def after(**kargs):                                                                                             
#    command = kargs.get("command")                                                                              
#    app = kargs.get("app")                                                                                      
#    args = kargs.get("args")                                                                                    
#    env = kargs.get("env")                                                                                      
#    # what to do this? bran                                                                                     

def run10(cmd):
    print "run10............."
    check_application()
    do_classpath()
    do_java('com.pxl.demo.DemoCommends')
    print "~ Ctrl+C to stop"
    java_cmd.append(cmd)
    subprocess.call(java_cmd, env=os.environ)
    print
    sys.exit(0)

if PLAY10:
    print "PLAY10............."
    if play_command == 'mdemo:gen':
        run10('gen')

    if play_command == 'mdemo:regen':
        run10('regen')
