<template>
  <div>
    <v-container>
      <v-select :items="types" v-model="type" label="认证方式" solo></v-select>

      <v-stepper v-model="step" vertical v-if="this.type">
        <v-stepper-step :complete="step > 1" step="1" editable>
          <b>基本信息</b>
          <small class="mt-1"
            >填写姓名、工作单位等基本信息;或在网站中寻找已存在的门户</small
          >
        </v-stepper-step>

        <v-stepper-content step="1">
          <v-card class="mb-12" outlined>
            <v-container v-if="type == '新建门户'">
              <div class="d-flex">
                <v-text-field
                  outlined
                  v-model="name"
                  label="姓名"
                  required
                ></v-text-field>
                <v-btn outlined @click="addIntrest()" height="56" class="mb-5">
                  添加研究方向
                </v-btn>
              </div>
              <div class="d-flex">
                <v-container v-for="i in interests.length" :key="i">
                  <v-text-field
                    outlined
                    label="研究方向"
                    v-model="interests[i - 1]"
                    append-icon="mdi-close"
                    @click:append="deleteIntrest(i - 1)"
                  >
                  </v-text-field>
                </v-container>
              </div>

              <v-row>
                <v-col cols="4">
                  <v-text-field
                    outlined
                    v-model="gIndex"
                    label="g指数"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="4">
                  <v-text-field
                    outlined
                    v-model="hIndex"
                    label="h指数"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row class="d-flex pa-5">
                <v-btn
                  @click="find('机构', 'cur')"
                  outlined
                  height="56"
                  class="px-2"
                >
                  查找现工作单位
                </v-btn>
                <v-switch
                  class="px-2"
                  inset
                  v-model="editCur"
                  label="自行编辑工作单位"
                  hide-details
                ></v-switch>
                <v-text-field
                  class="px-2"
                  outlined
                  v-model="currentInst.name"
                  label="现工作单位名称"
                  hide-details
                  :disabled="!editCur"
                  required
                ></v-text-field>
              </v-row>
              <v-row
                v-for="i in institutions.length"
                :key="i"
                class="d-flex pa-5"
              >
                <v-btn
                  class="px-2"
                  @click="find('机构', 'ins' + (i - 1))"
                  outlined
                  height="56"
                >
                  查找曾工作单位
                </v-btn>
                <v-switch
                  class="px-2"
                  inset
                  v-model="editIns[i - 1]"
                  label="自行编辑工作单位"
                  hide-details
                ></v-switch>
                <v-text-field
                  class="px-2"
                  outlined
                  v-model="institutions[i - 1].name"
                  label="曾工作单位名称"
                  :disabled="!editIns[i - 1]"
                  hide-details
                  required
                  append-icon="mdi-close"
                  @click:append="deleteI(i - 1)"
                ></v-text-field>
              </v-row>
              <v-btn @click="addInst()" outlined height="56">
                新增曾工作单位
              </v-btn>
            </v-container>
            <v-container v-if="type == '认领已有门户'">
              <v-row
                v-for="i in portals.length"
                :key="i"
                class="d-flex justify-space-around mb-5"
              >
                <p>{{ portalsName[i - 1] }}</p>
                <v-btn @click="find('科研人员', 'aut' + (i - 1))" outlined>
                  查找门户
                </v-btn>
                <v-btn @click="deleteP(i - 1)" outlined> 删除该项 </v-btn>
              </v-row>
              <v-btn @click="addProtal()" outlined> 添加认领门户 </v-btn>
            </v-container>
          </v-card>
          <v-btn color="primary" @click="step = 2"> 继续 </v-btn>
        </v-stepper-content>

        <v-stepper-step :complete="step > 2" step="2" editable>
          <b>上传资料</b>
          <small class="mt-1">上传证明材料或网页证明链接</small>
        </v-stepper-step>

        <v-stepper-content step="2">
          <v-card class="mb-12" height="200px" outlined>
            <v-container>
              <v-text-field
                outlined
                v-model="websiteLink"
                label="证明材料链接"
              ></v-text-field>
              <div class="d-flex">
                <v-file-input
                  outlined
                  class="file"
                  chips
                  label="上传证明文件"
                  @change="selectFile"
                ></v-file-input>
                <v-btn outlined height="56" @click="upload()">上传</v-btn>
              </div>
            </v-container>
          </v-card>
          <v-btn color="primary" @click="step = 3"> 继续 </v-btn>
          <v-btn text @click="step = 1"> 返回 </v-btn>
        </v-stepper-content>

        <v-stepper-step step="3" editable>
          <b>邮箱验证</b>
        </v-stepper-step>

        <v-stepper-content step="3">
          <v-card class="mb-12" height="200px" outlined>
            <v-container>
              <div class="d-flex">
                <v-text-field
                  outlined
                  v-model="email"
                  label="邮箱"
                  :rules="emailRules"
                  required
                ></v-text-field>
                <v-btn
                  outlined
                  height="56"
                  @click="getVertifyCode()"
                  v-if="time == 0"
                >
                  发送验证码
                </v-btn>
                <v-btn outlined disabled v-else> 发送验证码({{ time }}) </v-btn>
              </div>
              <v-text-field
                outlined
                v-model="vertifyCode"
                label="验证码"
                required
              ></v-text-field>
            </v-container>
          </v-card>
          <v-btn color="primary" @click="submit()"> 提交 </v-btn>
          <v-btn text @click="step = 2"> 返回 </v-btn>
        </v-stepper-content>
      </v-stepper>
      <div v-if="type == '认领已有门户'"></div>
    </v-container>

    <v-dialog v-model="getID" persistent width="1500px">
      <v-card height="5000px">
        <Search
          :key="timer"
          :fromDoor="disabled"
          :todo="todo"
          @closeID="closeID"
          @findResult="findResult"
        ></Search>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import Search from "../Search.vue";
export default {
  components: { Search },
  data() {
    return {
      getID: false,
      type: "",
      types: ["新建门户", "认领已有门户"],
      id: "",
      name: "",
      position: "",
      interests: [],
      email: "",
      gIndex: null,
      hIndex: null,
      paperNum: 0,
      patentNum: 0,
      avatarUrl: "",
      websiteLink: null,
      fileToken: null,
      vertifyCode: "",
      time: 0,
      portals: [],
      portalsName: [],
      snackbarEmail: false,
      snackbarSub: false,
      currentInst: {
        id: null,
        name: null,
      },
      institutions: [],
      institutionNum: 0,
      // idRules: [(v) => !!v || "请填写姓名"],
      // passwordRules: [(v) => !!v || "请填写密码"],
      emailRules: [
        (v) => !!v || "请填写邮箱",
        (v) => /.+@.+\..+/.test(v) || "邮箱格式不合法",
      ],
      todo: "全部",
      disabled: "disabled",
      editCur: false,
      editIns: [],
      itemGetM: null,
      timer: "",
      currentFile: null,
      step: 1,
    };
  },
  methods: {
    getVertifyCode() {
      if (/.*@.*/.test(this.email)) {
        this.$axios({
          method: "post",
          url: "/api/scholar/certify/code",
          params: {
            email: this.email,
          },
        })
          .then((response) => {
            console.log(response.data);
          })
          .catch((error) => {
            console.log(error);
          });
        this.time = 60;
        let timer = setInterval(() => {
          this.time -= 1;
          if (this.time == 0) {
            clearInterval(timer);
          }
        }, 1000);
      } else {
        this.snackbarEmail = true;
      }
    },
    addInst() {
      this.institutions.push({
        id: null,
        name: null,
      });
      this.editIns.push(false);
    },
    addProtal() {
      this.portals.push(null);
    },
    addIntrest() {
      this.interests.push("");
    },
    deleteI(index) {
      this.institutions.splice(index, 1);
      this.editIns.splice(index, 1);
    },
    deleteP(index) {
      this.portals.splice(index, 1);
    },
    deleteIntrest(index) {
      this.interests.splice(index, 1);
    },
    selectFile(file) {
      this.currentFile = file;
    },
    upload() {
      let formData = new window.FormData();
      formData.append("file", this.currentFile);
      if (this.fileToken != null) {
        formData.append("token", this.fileToken);
      }
      this.$axios({
        method: "post",
        url: "/api/resource/upload",
        data: formData,
      })
        .then((response) => {
          console.log(response.data);
          if (response.data.success) {
            this.fileToken = response.data.data;
            this.$notify({
              title: "upload",
              message: "上传成功",
              type: "success",
            });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    submit() {
      if (this.email == "") {
        this.$notify({
          title: "失败",
          message: "请输入邮箱",
          type: "error",
        });
        return;
      } else if (this.websiteLink == null && this.fileToken == null) {
        this.$notify({
          title: "失败",
          message: "网址证明和文件证明至少需要一个",
          type: "error",
        });
        return;
      } else if (this.vertifyCode == "") {
        this.$notify({
          title: "失败",
          message: "请输入验证码",
          type: "error",
        });
        return;
      }
      var claim = {
        portals: this.portals,
      };
      var create = {
        currentInst: {
          id: this.currentInst.id,
          name: this.currentInst.name,
        },
        gIndex: this.gIndex,
        hIndex: this.hIndex,
        institutions: this.institutions,
        interests: this.interests,
        name: this.name,
      };
      if (this.type == "新建门户") {
        claim = null;
        if (this.editCur) {
          create.currentInst.id = null;
        } else {
          create.currentInst.name = null;
        }
        let i = 0;
        for (i = 0; i < create.institutions.length; i++) {
          if (this.editIns[i]) {
            create.institutions[i].id = null;
          } else {
            create.institutions[i].name = null;
          }
        }
      } else if (this.type == "认领已有门户") {
        create = null;
      }
      var b = {
        content: {
          claim: claim,
          code: this.vertifyCode,
          create: create,
        },
        email: this.email,
        fileToken: this.fileToken,
        websiteLink: this.websiteLink,
      };
      console.log(b);
      this.$axios({
        method: "post",
        url: "/api/scholar/certify",
        data: {
          content: {
            claim: claim,
            code: this.vertifyCode,
            create: create,
          },
          email: this.email,
          fileToken: this.fileToken,
          websiteLink: this.websiteLink,
        },
      })
        .then((response) => {
          console.log(response.data);
          if (response.data.success) {
            this.$notify({
              title: "成功",
              message: "申请成功",
              type: "success",
            });
          } else {
            this.$notify({
              title: "失败",
              message: "请核对信息完整程度，检查验证码是否正确",
              type: "error",
            });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    closeID(msg) {
      if (msg == "close") this.getID = false;
    },
    findResult(msg) {
      msg.name = msg.name.replaceAll("<b>", "");
      msg.name = msg.name.replaceAll("</b>", "");
      let str = this.itemGetM;
      if (str == "cur") {
        this.currentInst = msg;
      } else if (str.substring(0, 3) == "ins") {
        let x = Number(str.substring(3));
        this.institutions[x] = msg;
      } else if (str.substring(0, 3) == "aut") {
        let x = Number(str.substring(3));
        this.portals[x] = msg.id;
        this.portalsName[x] = msg.name;
      }
      this.$notify({
        title: "成功",
        message: "添加成功",
        type: "success",
      });
    },
    find(type, it) {
      this.todo = type;
      this.getID = true;
      this.itemGetM = it;
      this.timer = new Date().getTime();
    },
  },
};
</script>