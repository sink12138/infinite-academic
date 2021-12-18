<template>
  <v-card
    width="4000"
    flat
    outlined
  >
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="tasks"
        :loading="loading"
        :options.sync="options"
        show-expand
        class="task"
      >
        <template v-slot:top>
          <v-toolbar
            flat
          >
            <v-toolbar-title>定时任务管理</v-toolbar-title>
            <v-divider
              inset
              vertical
            ></v-divider>
            <v-spacer></v-spacer>

            <v-icon
              @click="getTasks"
            >
              mdi-refresh
            </v-icon>

            <v-dialog
              v-model="dialog"
              max-width="500px"
            >
              <v-card>
                <v-card-title>
                  <span class="text-h5">执行频率设置</span>
                </v-card-title>

                <v-card-text>
                  <v-container>
                    <v-slider
                      v-if="radioGroup == 0"
                      v-model="frequency"
                      step="1"
                      label="频率"
                      max="31"
                      min="1"
                      thumb-label
                      ticks
                    ></v-slider>
                    <v-slider
                      v-if="radioGroup == 1"
                      v-model="frequency"
                      step="1"
                      label="频率"
                      max="12"
                      min="1"
                      thumb-label
                      ticks
                    ></v-slider>

                    <v-radio-group v-model="radioGroup">
                      <v-radio
                        v-for="(item, id) in radio"
                        :key="id"
                        :label="item.title"
                        :value="item.num"
                      ></v-radio>
                    </v-radio-group>

                    <v-slider
                      v-model="hour"
                      step="1"
                      label="时间"
                      max="23"
                      min="0"
                      thumb-label
                      ticks
                    ></v-slider>

                    <p
                      v-if="radioGroup == 0"
                    >
                      频率：每{{ frequency }}天{{ hour }}点更新
                    </p>
                    <p
                      v-if="radioGroup == 1"
                    >
                      频率：每{{ frequency }}月{{ hour }}点更新
                    </p>
                  </v-container>
                </v-card-text>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn
                    color="blue darken-1"
                    :disabled="radioGroup < 0"
                    @click="set"
                  >
                    确定
                  </v-btn>

                  <v-btn
                    color="blue darken-1"
                    @click="close"
                  >
                    关闭
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-toolbar>
        </template>

        <template v-slot:expanded-item="{ headers, item }">
          <td :colspan="headers.length">
            <v-data-table
              :headers="childheaders"
              :items="item.tasks"
              hide-default-footer
              class="childtask"
            >
            </v-data-table>
          </td>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <v-icon
            @click="setItem(item)"
          >
            mdi-cogs
          </v-icon>
          <v-icon
            @click="runItem(item)"
            :disabled="item.running"
          >
            mdi-play-circle-outline
          </v-icon>
          <v-icon
            @click="stopItem(item)"
            :disabled="!item.running"
          >
            mdi-pause-circle-outline
          </v-icon>
        </template>

        <template v-slot:no-data>
          <v-btn
            color="primary"
            @click="getTasks"
          >
            重置
          </v-btn>
        </template>
      </v-data-table>
    </v-card-text>

    <v-divider></v-divider>

    <v-toolbar
      flat
    >
      <v-toolbar-title>爬虫灵感设置</v-toolbar-title>
      <v-divider
        inset
        vertical
      ></v-divider>
      <v-spacer></v-spacer>

      <v-col 
        cols="12"
        sm="3"
      >
        <v-text-field
          v-model="text"
          label="关键词"
          append-icon="mdi-plus"
          @click:append="addInspiration"
          outlined
          rounded
          dense
        ></v-text-field>
      </v-col>

      <v-btn
        class="ml-2"
        icon
        @click="setInspiration"
      >
        <v-icon>mdi-check-bold</v-icon>
      </v-btn>
      
    </v-toolbar>

    <v-card>
      <v-card-text>
        <v-chip-group
          active-class="primary--text"
          column
        >
          <v-chip
            v-for="inspiration in inspirations"
            :key="inspiration"
          >
            {{ inspiration }}
          </v-chip>
        </v-chip-group>
      </v-card-text>
    </v-card>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      dialog: false,
      loading: true,
      headers: [
        {
          text: '任务名称',
          align: 'start',
          value: 'name',
          sortable: false,
        },
        { text: '上次运行时间', value: 'lastRun', sortable: false },
        { text: '下次运行时间', value: 'nextRun', sortable: false },
        { text: '预定频率', value: 'frequency', sortable: false },
        { text: '运行状态', value: 'running', sortable: false },
        { text: '操作', value: 'actions', sortable: false },
      ],
      tasks: [],
      radioGroup: 0,
      radio: [
        {
          num: 0,
          title: '天'
        },
        {
          num: 1,
          title: '月'
        }
      ],
      frequency: 0,
      hour: 0,
      cron: '',
      options: {},
      inspirations: [],
      totalInsprations: 0,
      text: '',
      childheaders: [
        {
          text: '子任务名称',
          align: 'start',
          value: 'name',
          sortable: false,
        },   
        { text: '运行状态', value: 'status', sortable: false },
      ],
      code: '',
    }
  },
  computed: {
      
  },

  watch: {
    options: {
      handler () {
        this.getTasks()
      },
      deep: true,
    },
  },

  created () {
    
  },

  methods:{
    getTasks () {
      this.loading = true

      this.$axios({
        method: "get",
        url: "api/admin/system/schedules",
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.tasks = response.data.data.schedules
          this.loading = false
        } else {
          this.$notify({
            title: "失败",
            message: "账户信息获取失败",
            type: "warning",
          });
          this.loading = false
        }
      });
    },

    setItem (item) {
      this.radioGroup = 0
      this.frequency = 0
      this.hour = 0

      if (item.name == "学科话题热点关联分析") {
        this.code = "analysis"
      }
      else {
        this.code = "spider"
      }

      if (this.radioGroup == 0) {
        this.cron = '0 0 ' + this.hour + ' */' + this.frequency +' * ?'
      }
      else {
        this.cron = '0 0 ' + this.hour + ' * */' + this.frequency +' ?'
      }

      this.dialog = true
    },

    set () {
      this.$axios({
        method: "post",
        url: "api/admin/system/timing",
        params: {
          code: this.code,
          cron: this.cron,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "任务频率设置成功",
            type: "warning",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "任务频率设置失败",
            type: "warning",
          });
        }
      });
      this.dialog = false
    },

    runItem (item) {
      if (item.name == "学科话题热点关联分析") {
        this.code = "analysis"
      }
      else {
        this.code = "spider"
      }

      this.$axios({
        method: "post",
        url: "api/admin/system/start",
        params: {
          code: this.code,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "运行任务成功",
            type: "warning",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "运行任务失败",
            type: "warning",
          });
        }
      });
    },

    stopItem (item) {
      if (item.name == "学科话题热点关联分析") {
        this.code = "analysis"
      }
      else {
        this.code = "spider"
      }

      this.$axios({
        method: "post",
        url: "api/admin/system/stop",
        params: {
          code: this.code,
        },
      }).then((response) => {
        console.log(response.data);
        console.log(this.code)
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "停止任务成功",
            type: "warning",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "停止任务失败",
            type: "warning",
          });
        }
      });
    },

    close () {
      console.log(this.radioGroup)
      this.dialog = false
    },

    addInspiration () {
      this.inspirations[this.totalInsprations++] = this.text
      this.text = ''
    },

    setInspiration () {
      this.$axios({
        method: "post",
        url: "api/admin/system/inspire",
        data: JSON.stringify(this.inspirations),
      }).then((response) => {
        console.log(JSON.stringify(this.inspirations))
        console.log(response.data);
        if (response.data.success === true) {
          this.inspirations = []
          this.$notify({
            title: "成功",
            message: "爬虫灵感添加成功",
            type: "success",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "爬虫灵感添加失败",
            type: "warning",
          });
        }
      });
    }
  },
}
</script>

<style>

</style>