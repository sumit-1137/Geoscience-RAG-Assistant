package org.example.geoscienceragassistant.embedding;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;

import org.springframework.stereotype.Service;

import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final int EMBEDDING_SIZE = 384;

    private static final String MODEL_PATH =
            "models/all-MiniLM-L6-v2/model.onnx";

    private static final String TOKENIZER_PATH =
            "models/all-MiniLM-L6-v2/tokenizer.json";

    private OrtEnvironment environment;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    private boolean initialized = false;

    /**
     * ONNX Runtime is intentionally NOT initialized
     * when Spring creates this bean.
     *
     * This prevents ApplicationContext startup failure
     * if the native ONNX DLL has a problem.
     */
    private synchronized void initialize() throws Exception {

        if (initialized) {
            return;
        }

        System.out.println("Initializing ONNX embedding model...");

        environment = OrtEnvironment.getEnvironment();

        session = environment.createSession(
                MODEL_PATH,
                new OrtSession.SessionOptions()
        );

        tokenizer = HuggingFaceTokenizer.newInstance(
                Path.of(TOKENIZER_PATH)
        );

        initialized = true;

        System.out.println(
                "Embedding model loaded successfully"
        );
    }

    public float[] generateEmbedding(String text)
            throws Exception {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text cannot be empty"
            );
        }

        // Initialize only when embedding is actually requested
        initialize();

        var encoding = tokenizer.encode(text);

        long[] inputIds = encoding.getIds();

        long[] attentionMask =
                encoding.getAttentionMask();

        long[] tokenTypeIds =
                encoding.getTypeIds();

        try (
                OnnxTensor inputIdsTensor =
                        OnnxTensor.createTensor(
                                environment,
                                LongBuffer.wrap(inputIds),
                                new long[]{
                                        1,
                                        inputIds.length
                                }
                        );

                OnnxTensor attentionMaskTensor =
                        OnnxTensor.createTensor(
                                environment,
                                LongBuffer.wrap(attentionMask),
                                new long[]{
                                        1,
                                        attentionMask.length
                                }
                        );

                OnnxTensor tokenTypeIdsTensor =
                        OnnxTensor.createTensor(
                                environment,
                                LongBuffer.wrap(tokenTypeIds),
                                new long[]{
                                        1,
                                        tokenTypeIds.length
                                }
                        )
        ) {

            Map<String, OnnxTensor> inputs =
                    Map.of(
                            "input_ids",
                            inputIdsTensor,

                            "attention_mask",
                            attentionMaskTensor,

                            "token_type_ids",
                            tokenTypeIdsTensor
                    );

            try (OrtSession.Result result =
                         session.run(inputs)) {

                float[][][] tokenEmbeddings =
                        (float[][][]) result
                                .get(0)
                                .getValue();

                float[] embedding =
                        meanPooling(
                                tokenEmbeddings,
                                attentionMask
                        );

                normalize(embedding);

                return embedding;
            }
        }
    }

    private float[] meanPooling(
            float[][][] embeddings,
            long[] attentionMask) {

        float[] result =
                new float[EMBEDDING_SIZE];

        int tokenCount = 0;

        for (int i = 0;
             i < attentionMask.length;
             i++) {

            if (attentionMask[i] == 1) {

                tokenCount++;

                for (int j = 0;
                     j < EMBEDDING_SIZE;
                     j++) {

                    result[j] +=
                            embeddings[0][i][j];
                }
            }
        }

        if (tokenCount > 0) {

            for (int j = 0;
                 j < EMBEDDING_SIZE;
                 j++) {

                result[j] /=
                        tokenCount;
            }
        }

        return result;
    }

    private void normalize(float[] vector) {

        double magnitude = 0.0;

        for (float value : vector) {

            magnitude +=
                    value * value;
        }

        magnitude =
                Math.sqrt(magnitude);

        if (magnitude == 0) {
            return;
        }

        for (int i = 0;
             i < vector.length;
             i++) {

            vector[i] =
                    (float)
                            (vector[i] / magnitude);
        }
    }
}