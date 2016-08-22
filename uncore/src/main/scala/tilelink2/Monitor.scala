// See LICENSE for license details.

package uncore.tilelink2

import Chisel._
import chisel3.internal.sourceinfo.SourceInfo

object TLMonitor
{
  def legalizeA(bundle: TLBundleA, edge: TLEdgeOut, sourceInfo: SourceInfo) = {
    assert (TLMessages.isA(bundle.opcode), "'A' channel has invalid opcode")(sourceInfo)

    when (bundle.opcode === TLMessages.Acquire) {
      assert (edge.manager.supportsAcquire(bundle.address, bundle.size), "'A' channel carries Acquire type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel Acquire carries invalid source ID")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'A' channel Acquire smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel Acquire address not aligned to size")(sourceInfo)
      assert (TLPermissions.isGrow(bundle.param), "'A' channel Acquire carries invalid grow param")(sourceInfo)
      assert (bundle.wmask === SInt(-1).asUInt, "'A' channel Acquire contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.Get) {
      assert (edge.manager.supportsGet(bundle.address, bundle.size), "'A' channel carries Get type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel Get carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel Get address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'A' channel Get carries invalid param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel Get contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.PutFullData) {
      assert (edge.manager.supportsPutFull(bundle.address, bundle.size), "'A' channel carries PutFull type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel PutFull carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel PutFull address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'A' channel PutFull carries invalid param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel PutFull contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.PutPartialData) {
      assert (edge.manager.supportsPutPartial(bundle.address, bundle.size), "'A' channel carries PutPartial type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel PutPartial carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel PutPartial address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'A' channel PutPartial carries invalid param")(sourceInfo)
      assert ((bundle.wmask & ~edge.fullMask(bundle.address, bundle.size)) === UInt(0), "'A' channel PutPartial contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.ArithmeticData) {
      assert (edge.manager.supportsArithmetic(bundle.address, bundle.size), "'A' channel carries Arithmetic type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel Arithmetic carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel Arithmetic address not aligned to size")(sourceInfo)
      assert (TLAtomics.isArithmetic(bundle.param), "'A' channel Arithmetic carries invalid opcode param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel Arithmetic contains invalid wmask")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.LogicalData) {
      assert (edge.manager.supportsLogical(bundle.address, bundle.size), "'A' channel carries Logical type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel Logical carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel Logical address not aligned to size")(sourceInfo)
      assert (TLAtomics.isLogical(bundle.param), "'A' channel Logical carries invalid opcode param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel Logical contains invalid wmask")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.Hint) {
      assert (edge.manager.supportsHint(bundle.address), "'A' channel carries Hint type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'A' channel Hint carries invalid source ID")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'A' channel Hint address not aligned to size")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel Hint contains invalid wmask")(sourceInfo)
    }
  }

  def legalizeB(bundle: TLBundleB, edge: TLEdgeIn, sourceInfo: SourceInfo) = {
    assert (TLMessages.isB(bundle.opcode), "'B' channel has invalid opcode")(sourceInfo)

    when (bundle.opcode === TLMessages.Probe) {
      assert (edge.client.supportsProbe(bundle.source, bundle.size), "'B' channel carries Probe type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel Probe carries unmanaged address")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'B' channel Probe smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel Probe address not aligned to size")(sourceInfo)
      assert (TLPermissions.isCap(bundle.param), "'B' channel Probe carries invalid cap param")(sourceInfo)
      assert (bundle.wmask === SInt(-1).asUInt, "'B' channel Probe contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.Get) {
      assert (edge.client.supportsGet(bundle.source, bundle.size), "'B' channel carries Get type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel Get carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel Get address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'B' channel Get carries invalid param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'A' channel Get contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.PutFullData) {
      assert (edge.client.supportsPutFull(bundle.source, bundle.size), "'B' channel carries PutFull type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel PutFull carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel PutFull address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'B' channel PutFull carries invalid param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'B' channel PutFull contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.PutPartialData) {
      assert (edge.client.supportsPutPartial(bundle.source, bundle.size), "'B' channel carries PutPartial type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel PutPartial carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel PutPartial address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'B' channel PutPartial carries invalid param")(sourceInfo)
      assert ((bundle.wmask & ~edge.fullMask(bundle.address, bundle.size)) === UInt(0), "'B' channel PutPartial contains invalid wmask")(sourceInfo)
    }

    when (bundle.opcode === TLMessages.ArithmeticData) {
      assert (edge.client.supportsArithmetic(bundle.source, bundle.size), "'B' channel carries Arithmetic type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel Arithmetic carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel Arithmetic address not aligned to size")(sourceInfo)
      assert (TLAtomics.isArithmetic(bundle.param), "'B' channel Arithmetic carries invalid opcode param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'B' channel Arithmetic contains invalid wmask")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.LogicalData) {
      assert (edge.client.supportsLogical(bundle.source, bundle.size), "'B' channel carries Logical type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel Logical carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel Logical address not aligned to size")(sourceInfo)
      assert (TLAtomics.isLogical(bundle.param), "'B' channel Logical carries invalid opcode param")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'B' channel Logical contains invalid wmask")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.Hint) {
      assert (edge.client.supportsHint(bundle.source), "'B' channel carries Hint type unsupported by client")(sourceInfo)
      assert (edge.manager.contains(bundle.address), "'B' channel Hint carries unmanaged address")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'B' channel Hint address not aligned to size")(sourceInfo)
      assert (bundle.wmask === edge.fullMask(bundle.address, bundle.size), "'B' channel Hint contains invalid wmask")(sourceInfo)
    }
  }

  def legalizeC(bundle: TLBundleC, edge: TLEdgeOut, sourceInfo: SourceInfo) = {
    assert (TLMessages.isC(bundle.opcode), "'C' channel has invalid opcode")(sourceInfo)
    
    when (bundle.opcode === TLMessages.ProbeAck) {
      assert (edge.manager.contains(bundle.address), "'C' channel ProbeAck carries unmanaged address")(sourceInfo)
      // source is ignored
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ProbeAck smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel ProbeAck address not aligned to size")(sourceInfo)
      assert (TLPermissions.isReport(bundle.param), "'C' channel ProbeAck carries invalid report param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.ProbeAckData) {
      assert (edge.manager.contains(bundle.address), "'C' channel ProbeAckData carries unmanaged address")(sourceInfo)
      // source is ignored
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ProbeAckData smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel ProbeAckData address not aligned to size")(sourceInfo)
      assert (TLPermissions.isReport(bundle.param), "'C' channel ProbeAckData carries invalid report param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.Release) {
      assert (edge.manager.supportsAcquire(bundle.address, bundle.size), "'C' channel carries Release type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'C' channel Release carries invalid source ID")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel Release smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel Release address not aligned to size")(sourceInfo)
      assert (TLPermissions.isShrink(bundle.param), "'C' channel Release carries invalid shrink param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.ReleaseData) {
      assert (edge.manager.supportsAcquire(bundle.address, bundle.size), "'C' channel carries ReleaseData type unsupported by manager")(sourceInfo)
      assert (edge.client.contains(bundle.source), "'C' channel ReleaseData carries invalid source ID")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ReleaseData smaller than a beat")(sourceInfo)
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel ReleaseData address not aligned to size")(sourceInfo)
      assert (TLPermissions.isShrink(bundle.param), "'C' channel ReleaseData carries invalid shrink param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAck) {
      assert (edge.manager.contains(bundle.address), "'C' channel AccessAck carries unmanaged address")(sourceInfo)
      // source is ignored
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel AccessAck address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'C' channel AccessAck carries invalid param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAckError) {
      assert (edge.manager.contains(bundle.address), "'C' channel AccessAckError carries unmanaged address")(sourceInfo)
      // source is ignored
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel AccessAckError address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'C' channel AccessAckError carries invalid param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAckData) {
      assert (edge.manager.contains(bundle.address), "'C' channel AccessAckData carries unmanaged address")(sourceInfo)
      // source is ignored
      assert (edge.isAligned(bundle.address, bundle.size), "'C' channel AccessAckData address not aligned to size")(sourceInfo)
      assert (bundle.param === UInt(0), "'C' channel AccessAckData carries invalid param")(sourceInfo)
    }
  }

  def legalizeD(bundle: TLBundleD, edge: TLEdgeIn, sourceInfo: SourceInfo) = {
    assert (TLMessages.isD(bundle.opcode), "'D' channel has invalid opcode")(sourceInfo)
    
    when (bundle.opcode === TLMessages.ReleaseAck) {
      assert (edge.client.contains(bundle.source), "'D' channel ReleaseAck carries invalid source ID")(sourceInfo)
      // sink is ignored
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel ReleaseAck smaller than a beat")(sourceInfo)
      assert (bundle.param === UInt(0), "'D' channel ReleaseeAck carries invalid param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.Grant) {
      assert (edge.client.contains(bundle.source), "'D' channel Grant carries invalid source ID")(sourceInfo)
      assert (edge.manager.contains(bundle.sink), "'D' channel Grant carries invalid sink ID")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel Grant smaller than a beat")(sourceInfo)
      assert (TLPermissions.isCap(bundle.param), "'D' channel Grant carries invalid cap param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.GrantData) {
      assert (edge.client.contains(bundle.source), "'D' channel GrantData carries invalid source ID")(sourceInfo)
      assert (edge.manager.contains(bundle.sink), "'D' channel GrantData carries invalid sink ID")(sourceInfo)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel GrantData smaller than a beat")(sourceInfo)
      assert (TLPermissions.isCap(bundle.param), "'D' channel GrantData carries invalid cap param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAck) {
      assert (edge.client.contains(bundle.source), "'D' channel AccessAck carries invalid source ID")(sourceInfo)
      // sink is ignored
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel AccessAck carries invalid param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAckError) {
      assert (edge.client.contains(bundle.source), "'D' channel AccessAckError carries invalid source ID")(sourceInfo)
      // sink is ignored
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel AccessAckError carries invalid param")(sourceInfo)
    }
    
    when (bundle.opcode === TLMessages.AccessAckData) {
      assert (edge.client.contains(bundle.source), "'D' channel AccessAckData carries invalid source ID")(sourceInfo)
      // sink is ignored
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel AccessAckData carries invalid param")(sourceInfo)
    }
  }

  def legalizeE(bundle: TLBundleE, edge: TLEdgeOut, sourceInfo: SourceInfo) = {
    assert (edge.manager.containsById(bundle.sink), "'E' channels carries invalid sink ID")(sourceInfo)
  }
  
  def legalize(bundleOut: TLBundle, edgeOut: TLEdgeOut, bundleIn: TLBundle, edgeIn: TLEdgeIn, sourceInfo: SourceInfo) = {
    when (bundleOut.a.valid) { legalizeA(bundleOut.a.bits, edgeOut, sourceInfo) }
    when (bundleIn .b.valid) { legalizeB(bundleIn .b.bits, edgeIn,  sourceInfo) }
    when (bundleOut.c.valid) { legalizeC(bundleOut.c.bits, edgeOut, sourceInfo) }
    when (bundleIn .d.valid) { legalizeD(bundleIn .d.bits, edgeIn,  sourceInfo) }
    when (bundleOut.e.valid) { legalizeE(bundleOut.e.bits, edgeOut, sourceInfo) }
  }
}
