NES emulator

## Running

To run the project, clone the project and run:
    
    cd nes_emu && mvn clean compile assembly:single
    # Run project via debugger    
    java -cp target/nes_emu-1.0-jar-with-dependencies.jar ffdYKJisu.nes_emu.main.ConsoleDebugger