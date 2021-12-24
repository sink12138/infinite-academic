<template>
  <div>
    <Banner :title="{ text: 'Portal', icon: 'mdi-clipboard-account' }"></Banner>
    <div class="whole">
      <v-row>
        <v-col cols="2">
          <v-card
            class="mx-auto left"
            width="256"
            height="600"
            tile
          >
            <v-navigation-drawer permanent>
              <v-system-bar></v-system-bar>
              <v-list>
                <v-list-item>
                  <v-list-item-avatar>
                    <v-img :src="avatarUrl"></v-img>
                  </v-list-item-avatar>
                </v-list-item>

                <v-list-item link>
                  <v-list-item-content>
                    <v-list-item-title class="title">{{name}}</v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
              <v-divider></v-divider>
              <v-list
                nav
                dense
              >
                <v-list-item-group color="primary">
                  <v-list-item @click="manage=0">
                    <v-list-group
                    >
                      <template v-slot:activator>
                        <v-list-item-content>
                          <v-list-item-title>门户管理</v-list-item-title>
                        </v-list-item-content>
                      </template>
                      <v-list-item link>
                        <v-list-item-title
                          @click="addDoor=true"
                          :disabled="editingD"
                        >认领门户</v-list-item-title>
                      </v-list-item>
                      <v-list-item link>
                        <v-list-item-title
                          @click="editDoor()"
                          :disabled="editingD"
                        >编辑门户信息</v-list-item-title>
                      </v-list-item>
                    </v-list-group>
                  </v-list-item>
                  <v-list-item @click="manage=1">
                    <v-list-group
                    >
                      <template v-slot:activator>
                        <v-list-item-content>
                          <v-list-item-title>论文管理</v-list-item-title>
                        </v-list-item-content>
                      </template>
                      <v-list-item link>
                        <v-list-item-title
                          @click="addingPaper=true"
                        >添加论文</v-list-item-title>
                      </v-list-item>
                    </v-list-group>
                  </v-list-item>
                </v-list-item-group>
              </v-list>
            </v-navigation-drawer>
          </v-card>
        </v-col>
        <v-col v-if="manage==0" offset-md="1">
          <div style="padding-top:10px;">
            <div style="padding-top:50px;">
              <v-card>
                <v-card-text>
                  <h1 style="text-align:left">{{name}}</h1>
                  <h3 style="text-align:left" v-for="i in interests.length" :key="i">科研方向:{{interests[i-1]}}&emsp;&emsp;</h3>
                  <h3 style="text-align:left">邮箱:{{email}}</h3>
                  <h3 style="text-align:left">g指数:{{gIndex}}  h指数:{{hIndex}}</h3>
                  <h3 style="text-align:left">文章数量:{{paperNum}}  专利数量:{{patentNum}}  引用次数:{{citationNum}}</h3>
                  <h3 style="text-align:left">现工作单位:{{currentInst.name}}</h3>
                  <v-row no-gutters>
                    <v-col cols=auto>
                      <h3 style="text-align:left">曾工作单位:</h3>
                    </v-col>
                    <v-col  cols=auto>
                      <div v-for="i in institutions.length" :key="i">
                        <h3 style="text-align:left">{{institutions[i-1].name}}</h3>
                      </div>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
            </div>
          </div>
        </v-col>
        <v-col v-if="manage==1" offset-md="1">
          <div style="padding-top:10px;">
            <v-row
              v-for="i in onePageNum"
              :key="i"
            >
              <!-- <div > -->
                <v-col cols="9" v-if="(i+(page-1)*onePageNum-1)<papers.length">
                  <CardPaper :item="papers[i+(page-1)*onePageNum-1]"></CardPaper>
                </v-col>
                <v-col v-if="(i+(page-1)*onePageNum-1)<papers.length">
                  <br/>
                  <br/>
                  <v-row>
                    <v-col>
                      <v-btn @click="editPaper(i+(page-1)*onePageNum-1)">
                        编辑文献
                      </v-btn>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col>
                      <v-btn @click="deletePaper(i+(page-1)*onePageNum-1)">
                        删除文献
                      </v-btn>
                    </v-col>
                  </v-row>

                </v-col>
              <!-- </div> -->

            </v-row>
            <v-row>
              <v-col>
                <v-pagination
                  v-model="page"
                  class="my-4"
                  :length="8>pageNum?pageNum:8"
                ></v-pagination>
              </v-col>
            </v-row>
          </div>

        </v-col>
      </v-row>
    </div>

    <!-- 修改信息 -->
    <v-dialog v-model="editingD" persistent width=800px >
    <v-card>
      <div class="whole">
            <v-row>
              <v-col cols="9">
                <v-text-field
                  v-model="emailE"
                  label="邮箱"
                  :rules="emailRules"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="nameE"
                  :rules="idRules"
                  label="姓名"
                  required
                ></v-text-field>
                </v-col>
            </v-row>
            <v-row>
              <v-col v-for="i in interestsE.length" :key="i" cols="6">
                <v-row>
                  <v-col>
                    <v-text-field
                      label="研究方向"
                      v-model="interestsE[i-1]"
                    ></v-text-field>
                  </v-col>
                  <v-col>
                    <v-btn @click="deleteIntrest(i-1)">
                      删除该项
                    </v-btn>
                  </v-col>
                </v-row>
              </v-col>
            </v-row>
            <v-row>
                <v-col>
                  <v-btn @click="addIntrest()">
                    添加研究方向
                  </v-btn>
                </v-col>
              </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="gIndexE"
                  label="g指数"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-text-field
                  v-model="hIndexE"
                  label="h指数"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="3">
                <br/>
                  <v-btn @click="find('机构','cur')">
                    查找现工作单位
                  </v-btn>
                </v-col>
                <v-col cols="3">
                  <v-switch v-model="editCur" label='自行编辑工作单位'></v-switch>
                </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="currentInstE.name"
                  label="现工作单位名称"
                  :disabled="!editCur"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row v-for="i in institutionsE.length" :key="i">
              <v-col cols="3">
                <br/>
                  <v-btn @click="find('机构','ins'+(i-1))">
                    查找曾工作单位
                  </v-btn>
                </v-col>
                <v-col cols="3">
                  <v-switch v-model="editIns[i-1]" label='自行编辑工作单位'></v-switch>
                </v-col>
              <v-col cols="4">
                <v-text-field
                  v-model="institutionsE[i-1].name"
                  label="曾工作单位名称"
                  :disabled="!editIns[i-1]"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="2">
                <v-btn @click="deleteI(i-1)">
                  删除该项
                </v-btn>
              </v-col>
            </v-row>
            <v-btn @click="addInst()">
              新增曾工作单位
            </v-btn>
            <v-row>
              <v-col >
                <v-textarea
                  label="修改说明"
                  v-model="description"
                ></v-textarea>
              </v-col>
            </v-row>
            <v-row>
              <v-col >
                <v-text-field
                  v-model="websiteLink"
                  label="证明材料链接"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="9">
                <v-file-input
                  class="file"
                  chips
                  label="上传证明文件"
                  @change="selectFile"
                ></v-file-input>
              </v-col>
              <v-col>
                <v-btn @click="upload()">上传</v-btn>
              </v-col>
            </v-row>
            <v-row v-if="editingD">
              <v-col cols="6">
                <v-btn v-if="editingD" @click="submit()">
                  提交修改信息
                </v-btn>
              </v-col>
              <v-col cols="6">
                <v-btn v-if="editingD" @click="cancelAll()">
                  取消
                </v-btn>
              </v-col>
            </v-row>

      </div>
    </v-card>
    </v-dialog>
    <!-- 认领门户 -->
    <v-dialog v-model="addDoor" persistent width=800px >
      <v-card>
        <div class="whole">
              <v-row v-for="i in portals.length" :key="i">
                <v-col cols="3">
                  <p>{{portalsName[i-1]}}</p>
                </v-col>
                <v-col cols="3">
                  <v-btn @click="find('科研人员','aut'+(i-1))">
                    查找门户
                  </v-btn>
                </v-col>
                <v-col cols="2">
                  <v-btn @click="deletePortal(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
              <v-btn @click="addProtal()">
                添加希望认领的门户
              </v-btn>
              <v-row>
                <v-col >
                  <v-text-field
                    v-model="websiteLink"
                    label="证明材料链接"
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="9">
                  <v-file-input
                    class="file"
                    chips
                    label="上传证明文件"
                    @change="selectFile"
                  ></v-file-input>
                </v-col>
                <v-col>
                  <v-btn @click="upload()">上传</v-btn>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="6">
                  <v-btn @click="submitAddDoor()">
                    提交认领信息
                  </v-btn>
                </v-col>
                <v-col cols="6">
                  <v-btn @click="cancelAll()">
                    取消
                  </v-btn>
                </v-col>
              </v-row>

        </div>

      </v-card>
    </v-dialog>
    <!-- 删除文献 -->
    <v-dialog v-model="deletingPaper" persistent width=800px >
      <v-card>
        <div class="whole">
          <v-row>
            <v-col cols="10">
              <h2>移除文献:{{theDelete.title}}</h2>
              <v-row>
                <v-col >
                  <v-textarea
                    label="删除原因"
                    v-model="deletePaperReason"
                  ></v-textarea>
                </v-col>
              </v-row>
              <v-row>
                <v-col >
                  <v-text-field
                    v-model="websiteLink"
                    label="证明材料链接"
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="9">
                  <v-file-input
                    class="file"
                    chips
                    label="上传证明文件"
                    @change="selectFile"
                  ></v-file-input>
                </v-col>
                <v-col>
                  <v-btn @click="upload()">上传</v-btn>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="6">
                  <v-btn @click="submitDeletePaper()">
                    提交认领信息
                  </v-btn>
                </v-col>
                <v-col cols="6">
                  <v-btn @click="cancelAll()">
                    取消
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </v-row>

        </div>

      </v-card>
    </v-dialog>
    <!-- 新增文献 -->
    <v-dialog v-model="addingPaper" persistent width=800px>
      <BaseEditPaper
      :paper="newPaper"
      :edit=false
      :email="email"
      :portalid="id"
      @closeZ="closeF"
      ></BaseEditPaper>
    </v-dialog>
    <!-- 修改文献 -->
    <v-dialog v-model="editingPaper" persistent width=800px>
      <BaseEditPaper
      :paper="thePaper"
      :edit=true
      :email="email"
      :portalid="id"
      @closeZ="closeF"
      ></BaseEditPaper>
    </v-dialog>
    <!-- ID -->
    <v-dialog v-model="getID" persistent width=1500px >
      <v-card height=5000px>
        <Search
          :fromDoor="disabled"
          :todo="todo"
          :key="timer"
          @closeID="closeID"
          @findResult="findResult"
        ></Search>
      </v-card>
    </v-dialog>


  </div>


</template>

<script>
import CardPaper from '../components/card/CardPaper.vue'
import BaseEditPaper from './BaseEditPaper.vue'
import Search from './Search.vue'
import Banner from "../components/BaseBanner.vue";
  export default {
    components: {CardPaper,BaseEditPaper,Search,Banner},
    data: () => ({
      //门户部分
      editingD:false,
      addDoor:false,
      deletingPaper:false,
      editingPaper:false,
      addingPaper:false,
      getID:false,
      deletePaperID:"",
      deletePaperReason:"",
      manage:0,
      id:"",
      name:"",
      nameE:"",
      position:"",
      interests:[],
      interestsE:[],
      email:"",
      emailE:"",
      gIndex:"",
      hIndex:"",
      gIndexE:"",
      hIndexE:"",
      paperNum:0,
      patentNum:0,
      citationNum:0,
      avatarUrl:"",
      websiteLink:null,
      fileToken:null,
      vertifyCode:"",
      description:"",
      time:0,
      portals:[],
      portalsName:[],
      addDoorID:"",
      snackbarEmail:false,
      snackbarSub:false,
      currentInst:{
        id:"",
        name:""
      },
      currentInstE:{
        id:"",
        name:""
      },
      institutions:[
        {
          id:"1",
          name:"a"
        },
        {
          id:"2",
          name:"b"
        }
      ],

      institutionsE:[],
      institutionNum:0,
      idRules: [(v) => !!v || "请填写姓名"],
      passwordRules: [(v) => !!v || "请填写密码"],
      emailRules: [
        (v) => !!v || "请填写邮箱",
        (v) => /.+@.+\..+/.test(v) || "邮箱格式不合法",
      ],

      //文献部分
      papers: [
        {
          "abstract": "假装这是一大段摘要",
          "authors": [
            {
              "id": "GF_4ynwBF-Mu8unTG1hc",
              "name": "谭火彬"
            }
          ],
          "citationNum": 114,
          "date": "2021-10-15",
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "journal": {
            "id": "GF_4ynwBF-Mu8unTG1hc",
            "title": "Science"
          },
          "keywords": [12,23,34],
          "title": "基于机器学习的无需人工编制词典的切词系统",
          "type": "期刊论文"
        },
        {
          "abstract": "假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要",
          "authors": [
            {
              "id": "GF_4ynwBF-Mu8unTG1hc",
              "name": "谭火彬"
            }
          ],
          "citationNum": 114,
          "date": "2021-10-15",
          "doi":"1234123",
          institutions:[
            {
              id:"in1",
              name:"ins1"
            }
          ],
          publisher:"asda",
          referencePapers:[
            {
              id:"1",
              title:"rep1"
            }
          ],
          "subjects":["ma","ca","ph"],
          "id": "GF_4ynwBF-Mu8unTG1hd",
          "journal": {
            "id": "GF_4ynwBF-Mu8unTG1hc",
            "title": "Science"
          },
          "keywords": ["dasd","asdf","agagd","dasd"],
          "title": "基于机器学习的无需人工编制词典的切词系统",
          "type": "期刊论文"
        },
      ],
      newPaper:{
        title:"",
        id:"",
        abstract:"",
        authors:[],
        date:"",
        doi:null,//
        institutions:[],//
        journal:Object,
        keywords:[],
        publisher:null,//
        referencePapers:[],//
        subjects:[],//
        type:"",
        year:null
      },
      thePaper:Object,
      theDelete:"",
      page:1,
      onePageNum:2,
      pageNum:2,
      currentFile:null,

      //找id
      disabled:"disabled",
      todo:"全部",
      editCur:false,
      editIns:[],
      itemGetM:null,
      timer:"",
    }),
    mounted(){
      this.$axios({
        method: "get",
        url: "/api/account/profile",
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.email = response.data.data.email;
          this.username = response.data.data.username;
          this.id=response.data.data.researcherId
          this.getInfo()
          this.getPapers()
        }
      });
    },
    methods:{
      mDoor(){
        this.manage=0
      },
      mPaper(){
        this.manage=1
      },
      getVertifyCode(){
        if(/.*@.*/.test(this.email)){
          this.$axios({
            method: "post",
            url: "/api/scholar/certify/code",
            params: {
              email: this.email
            }
          }).then(response => {
            console.log(response.data)
          }).catch(error => {
            console.log(error)
          })
            this.time=60
            let timer = setInterval(()=>{
              this.time-=1
              if (this.time==0) {
                clearInterval(timer);
              }
            },1000)
        }else{
          this.snackbarEmail=true
        }

      },
      addInst(){
        this.institutionsE.push({
          id:"",
          name:""
        })
        this.editIns.push(false)
      },
      addProtal(){
        this.portals.push("")
        this.portalsName.push("")
      },
      addIntrest(){
        this.interestsE.push("")
      },
      deleteI(index){
        this.institutionsE.splice(index, 1)
      },
      deletePortal(index){
        this.portals.splice(index, 1)
        this.portalsName.splice(index, 1)
      },
      deletePaper(index){
        this.deletingPaper=true
        this.theDelete=this.papers[index]
      },
      deleteIntrest(index){
        this.interestsE.splice(index,1)
        this.interests.splice(index,1)
      },
      editPaper(index){
        if(this.papers[index].subjects==null){
          this.papers[index].subjects=[]
        }
        if(this.papers[index].institutions==null){
          this.papers[index].institutions=[]
        }
        if(this.papers[index].authors==null){
          this.papers[index].authors=[]
        }
        if(this.papers[index].referencePapers==null){
          this.papers[index].referencePapers=[]
        }
        if(this.papers[index].journal==null){
          this.papers[index].journal={}
        }
        this.editingPaper=true
        this.thePaper=this.papers[index]
      },
      closeF(msg){
        if(msg=="close"){
          this.addingPaper=false
          this.editingPaper=false
        }
      },
      closeID(msg){
        if(msg=="close")
          this.getID=false
      },
      cancelAll(){
        this.editingD=false,
        this.addDoor=false,
        this.deletingPaper=false,
        this.clearWeb()
      },
      clearWeb(){
        this.websiteLink=null,
        this.vertifyCode="",
        this.fileToken=null
      },
      editDoor(){
        this.editingD=true
        this.nameE=this.name
        this.emailE=this.email
        this.interestsE=this.interests
        this.gIndexE=this.gIndex
        this.hIndexE=this.hIndex
        this.currentInstE=JSON.parse( JSON.stringify(this.currentInst) )
        this.institutionsE=JSON.parse( JSON.stringify(this.institutions) )
      },
      selectFile(file){
        this.currentFile = file;
      },
      upload(){
        let formData = new window.FormData();
        formData.append("file", this.currentFile);
        if(this.fileToken!=null){
          formData.append("token",this.fileToken)
        }
        console.log(formData)
        this.$axios({
          method: "post",
          url: "/api/resource/upload",
          data: formData,
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.fileToken=response.data.data
            this.$notify({
              title: 'upload',
              message: "上传成功",
              type: 'success'
            });
          }
        }).catch(error => {
          console.log(error)
        })
      },
      submit(){
        if(this.email==''){
          this.$notify({
            title: "失败",
            message: "请输入邮箱",
            type: "error",
          });
          return
        }else if(this.websiteLink==null&&this.fileToken==null){
          this.$notify({
            title: "失败",
            message: "网址证明和文件证明至少需要一个",
            type: "error",
          });
          return
        }
        let d={
          content:{
              description:this.description,
              info:{
                currentInst:{
                  id:this.currentInstE.id,
                  name:this.currentInstE.name
                },
                gIndex:this.gIndexE,
                hIndex:this.hIndexE,
                institutions:this.institutionsE,
                interests:this.interestsE,
                name:this.nameE,
              }
            },
            email:this.emailE,
            fileToken:this.fileToken,
            websiteLink:this.websiteLink
        }
        if(this.editCur){
          d.content.info.currentInst.id=null
        }else{
          d.content.info.currentInst.name=null
        }
        let i=0
        for(i=0;i<d.content.info.institutions.length;i++){
          if(this.editIns[i]){
            d.content.info.institutions[i].id=null
          }else{
            d.content.info.institutions[i].name=null
          }
        }
        this.$axios({
          method: "post",
          url: "/api/scholar/modify",
          data:d
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.snackbarSub=true
            this.editingD=false
            this.clearWeb()
            this.$notify({
              title: "成功",
              message: "申请成功",
              type: "success",
            });
          }else{
            this.$notify({
              title: "失败",
              message: response.data.message,
              type: "error",
            });
          }

        }).catch(error => {
          console.log(error)
        })
      },
      submitAddDoor(){
        if(this.websiteLink==null&&this.fileToken==null){
          this.$notify({
            title: "失败",
            message: "网址证明和文件证明至少需要一个",
            type: "error",
          });
          return
        }
        let dat={
          content:{
            portals:this.portals,
          },
          email:this.emailE,
          fileToken:this.fileToken,
          websiteLink:this.websiteLink
        }
        console.log(JSON.stringify(dat))
        this.$axios({
          method: "post",
          url: "/api/scholar/claim",
          data:dat
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.snackbarSub=true
            this.addDoor=false
            this.clearWeb()
            this.$notify({
              title: "成功",
              message: "申请成功",
              type: "success",
            });
          }else{
            this.$notify({
              title: "失败",
              message: response.data.message,
              type: "error",
            });
          }

        }).catch(error => {
          console.log(error)
        })
      },
      submitDeletePaper(){
        let data={
            content:{
              paperId:this.theDelete.id,
              reason:this.deletePaperReason
            },
            email:this.email,
            fileToken:this.fileToken,
            websiteLink:this.websiteLink
          }
        console.log(JSON.stringify(data))
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/remove",
          data: data
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.snackbarSub=true
            this.deletingPaper=false
            this.clearWeb()
            this.$notify({
              title: "成功",
              message: "申请成功",
              type: "success",
            });
          }else{
            this.$notify({
              title: "失败",
              message: "请核对信息完整程度",
              type: "error",
            });
          }

        }).catch(error => {
          console.log(error)
        })
      },
      getInfo(){
        console.log("api/search/info/researcher/"+this.id)
        this.$axios({
          method: "get",
          url: "api/search/info/researcher/"+this.id,
          params: {
            id: this.id
          }
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.avatarUrl=response.data.data.avatarUrl
            this.citationNum=response.data.data.citationNum
            this.name=response.data.data.name
            this.position=response.data.data.position
            this.interests=response.data.data.interests
            this.hIndex=response.data.data.gIndex
            this.paperNum=response.data.data.paperNum
            this.patentNum=response.data.data.patentNum
            this.currentInst=response.data.data.currentInst
            this.institutions=response.data.data.institutions
          }

        }).catch(error => {
          console.log(error)
        })
      },
      getPapers(){
        this.papers=[]
        let hasMore=false
        let page=0
        let i
        this.$axios({
          method: "get",
          url: "/api/search/relation/publications/researcher/"+this.id+"/"+page,
          data: {
            entity:"researcher",
            id: this.id,
            page:page,
          }
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            console.log(response.data.data.items)
            hasMore=response.data.data.hasMore
            for(i=0;i<response.data.data.items.length;i++){
              this.papers.push(response.data.data.items[i])
            }
            page++
            while (hasMore) {
                this.$axios({
                method: "get",
                url: "/search/relation/publications/researcher/"+this.id+"/"+page,
                params: {
                  entity:"researcher",
                  id: this.id,
                  page:page,
                }
              }).then(response => {
                console.log(response.data)
                if(response.data.success){
                  console.log(response.data.data.items)
                  hasMore=response.data.data.hasMore
                  for(i=0;i<response.data.data.items.length;i++){
                    this.papers.push(response.data.data.items[i])
                  }
                  page++
                }
              }).catch(error => {
                console.log(error)
              })
            }
          }

        }).catch(error => {
          console.log(error)
        })

        this.timer = setTimeout(()=>{   //设置延迟执行
          this.pageNum=Math.ceil(this.papers.length/this.onePageNum)
          console.log(this.pageNum)
          console.log(this.papers)
          console.log(this.papers.length)
        },1000);

      },
      findResult(msg){
        msg.name=msg.name.replaceAll('<b>','')
        msg.name=msg.name.replaceAll('</b>','')
        let str=this.itemGetM
        if(str=='cur'){
          this.currentInstE=msg
        }else if(str.substring(0,3)=='ins'){
          let x=Number(str.substring(3))
          this.institutionsE[x]=msg
        }else if(str.substring(0,3)=='aut'){
          let x=Number(str.substring(3))
          this.portals[x]=msg.id
          this.portalsName[x]=msg.name
        }
        this.$notify({
          title: "成功",
          message: "添加成功",
          type: "success",
        });
      },
      find(type,it){
        this.todo=type
        this.getID=true
        this.itemGetM=it
        this.timer=new Date().getTime()
      }
    }
  }
</script>
