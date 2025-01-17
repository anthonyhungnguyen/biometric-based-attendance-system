import BarChart from "components/Student/BarChart.jsx"
import MainLayout from "layouts/MainLayout"
import React, { useEffect } from "react"
import { Divider } from "antd"
import SemesterSelect from "components/SemesterSelect"
import { useDispatch, useSelector } from "react-redux"
import FaceProfile from "components/Student/FaceProfile.jsx"
import { logsRequest } from "slices/log"
import StudentLogs from "components/Student/StudentLogs.jsx"
import ROLE from "constants/role"

export default function Home() {
  const dispatch = useDispatch()
  const { user } = useSelector(state => state.auth)
  const { userid } = user
  useEffect(() => {
    dispatch(logsRequest(userid, ROLE.STUDENT))
  }, [userid, dispatch])
  return (
    <MainLayout>
      <div className="flex justify-between">
        <div className="font-semibold text-lg">Hi, {userid}</div>
        <SemesterSelect />
      </div>
      <Divider />
      <div className="flex flex-wrap justify-around">
        <div className="w-full">
          <BarChart />
        </div>
        <div className="md:w-2/5">
          <StudentLogs />
        </div>
        <div className="md:w-2/5">
          <FaceProfile />
        </div>
      </div>
    </MainLayout>
  )
}
