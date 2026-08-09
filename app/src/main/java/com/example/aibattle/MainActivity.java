package com.example.aibattle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI Battle Platform 主界面
 * 用户输入任务 → 多个AI模组并发响应 → 展示结果供用户选择
 */
public class MainActivity extends AppCompatActivity {

    private EditText taskInput;
    private Button startButton;
    private LinearLayout resultsContainer;
    private ExecutorService executor;
    private List<AIModule> modules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initModules();
        setupListeners();
    }

    private void initViews() {
        taskInput = findViewById(R.id.taskInput);
        startButton = findViewById(R.id.startButton);
        resultsContainer = findViewById(R.id.resultsContainer);
        executor = Executors.newFixedThreadPool(10);
    }

    private void initModules() {
        modules = new ArrayList<>();
        
        // 模拟注册多个AI模组
        modules.add(new MockModule("智谱清言", "GLM-4翻译模型"));
        modules.add(new MockModule("DeepSeek", "DeepSeek-Chat"));
        modules.add(new MockModule("通义千问", "Qwen-Turbo"));
        modules.add(new MockModule("Kimi", "Moonshot模型"));
        modules.add(new MockModule("本地模型", "Ollama运行"));
    }

    private void setupListeners() {
        startButton.setOnClickListener(v -> startBattle());
    }

    private void startBattle() {
        String task = taskInput.getText().toString().trim();
        if (task.isEmpty()) {
            Toast.makeText(this, "请输入任务内容", Toast.LENGTH_SHORT).show();
            return;
        }

        resultsContainer.removeAllViews();
        startButton.setEnabled(false);
        startButton.setText("竞技中...");

        // 并发执行所有模组
        for (AIModule module : modules) {
            executor.execute(() -> executeModule(module, task));
        }

        // 延迟恢复按钮
        startButton.postDelayed(() -> {
            startButton.setEnabled(true);
            startButton.setText("开始竞技");
        }, 3000);
    }

    private void executeModule(AIModule module, String task) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 模拟AI处理
            Thread.sleep(500 + (long) (Math.random() * 1500));
            
            String result = module.execute(task);
            long latency = System.currentTimeMillis() - startTime;
            
            // 显示结果
            runOnUiThread(() -> showResult(module.getName(), result, latency));
            
        } catch (Exception e) {
            runOnUiThread(() -> showError(module.getName(), e.getMessage()));
        }
    }

    private void showResult(String moduleName, String result, long latency) {
        View resultView = createResultView(moduleName, result, latency);
        resultsContainer.addView(resultView);
    }

    private View createResultView(String moduleName, String result, long latency) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        
        // 模块名称和延迟
        TextView header = new TextView(this);
        header.setText(String.format("%s (%dms)", moduleName, latency));
        header.setTextSize(16);
        header.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        layout.addView(header);

        // 结果内容
        TextView content = new TextView(this);
        content.setText(result);
        content.setTextSize(14);
        content.setPadding(0, 8, 0, 8);
        layout.addView(content);

        // 评分按钮
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        
        for (int i = 1; i <= 5; i++) {
            Button star = new Button(this);
            star.setText(String.valueOf(i));
            star.setOnClickListener(v -> {
                Toast.makeText(this, 
                    String.format("已为%s评分%d星", moduleName, i), 
                    Toast.LENGTH_SHORT).show();
            });
            buttons.addView(star);
        }
        
        layout.addView(buttons);
        
        // 分隔线
        View divider = new View(this);
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = 16;
        divider.setLayoutParams(params);
        layout.addView(divider);

        return layout;
    }

    private void showError(String moduleName, String error) {
        TextView errorView = new TextView(this);
        errorView.setText(String.format("%s: %s", moduleName, error));
        errorView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        resultsContainer.addView(errorView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}

/**
 * AI模组接口
 */
interface AIModule {
    String getName();
    String execute(String task);
}

/**
 * 模拟模组实现
 */
class MockModule implements AIModule {
    private String name;
    private String description;

    MockModule(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String execute(String task) {
        // 模拟AI处理逻辑
        return String.format("[%s处理结果]\n任务: %s\n\n这是一个模拟的翻译/润色结果。\n\n实际使用时可替换为真实API调用。", 
            name, task);
    }
}