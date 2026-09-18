package org.example.geoscienceragassistant;

import ai.onnxruntime.OrtEnvironment;

public class OnnxTest {

    public static void main(String[] args) {

        System.out.println("Java version: "
                + System.getProperty("java.version"));

        System.out.println("OS: "
                + System.getProperty("os.name"));

        System.out.println("Architecture: "
                + System.getProperty("os.arch"));

        try {

            OrtEnvironment environment =
                    OrtEnvironment.getEnvironment();

            System.out.println(
                    "ONNX Runtime initialized successfully!"
            );

            environment.close();

        } catch (Throwable e) {

            System.out.println(
                    "ONNX Runtime initialization FAILED"
            );

            e.printStackTrace();
        }
    }
}